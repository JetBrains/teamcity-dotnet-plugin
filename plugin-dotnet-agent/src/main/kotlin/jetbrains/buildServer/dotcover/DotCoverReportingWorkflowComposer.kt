package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.command.*
import jetbrains.buildServer.dotcover.report.DotCoverTeamCityReportGenerator
import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotcover.statistics.DotnetCoverageStatisticsPublisher
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_SNAPSHOT_DCVR
import jetbrains.buildServer.dotcover.report.artifacts.ArtifactsUploader
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.util.FileUtil.resolvePath
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.rx.use
import java.io.File
import kotlin.streams.toList

class DotCoverReportingWorkflowComposer(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverRunConfigFileSerializer: DotCoverRunConfigFileSerializer,
    private val _dotCoverResponseFileSerializer: DotCoverResponseFileSerializer,
    private val _loggerService: LoggerService,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
    private val _entryPointSelector: DotCoverEntryPointSelector,
    private val _dotCoverSettings: DotCoverSettings,
    private val _dotCoverTeamCityReportGenerator: DotCoverTeamCityReportGenerator,
    private val _dotnetCoverageStatisticsPublisher: DotnetCoverageStatisticsPublisher,
    private val _uploader: ArtifactsUploader,
    private val _dotCoverAgentTool : DotCoverAgentTool,
    dotCoverCommandLineBuildersList: List<DotCoverCommandLineBuilder>
) : BuildStepPostProcessingWorkflowComposer {

    private val _dotCoverCommandLineBuilders: Map<DotCoverCommandType, DotCoverCommandLineBuilder> =
        dotCoverCommandLineBuildersList.associateBy { it.type }
    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow {
        if (_dotCoverSettings.dotCoverMode.isDisabled) {
            return workflow
        }
        if (_dotCoverSettings.dotCoverHomePath.isNullOrBlank()) {
            _loggerService.writeWarning("Skip code coverage: dotCover is enabled however tool home path has not been set")
            return workflow
        }

        return sequence {
            val executablePath = getDotCoverExecutablePath()
            val tempDirectory = _pathsService.getPath(PathType.AgentTemp)

            // merge
            _dotCoverSettings.shouldMergeSnapshots().let { (shouldMergeSnapshots, logMessage) -> when {
                shouldMergeSnapshots -> merge(executablePath, tempDirectory)
                else -> _loggerService.writeDebug(logMessage)
            }}

            // report
            _dotCoverSettings.shouldGenerateReport().let { (shouldGenerateReport, logMessage) -> when {
                shouldGenerateReport -> report(executablePath, tempDirectory)
                else -> _loggerService.writeDebug(logMessage)
            }}
        }.let(::Workflow)
    }

    private fun getDotCoverExecutablePath(): Path = _entryPointSelector.select().fold(
        onSuccess = { return Path(_virtualContext.resolvePath(it.path)) },
        onFailure = { throw RunBuildException("dotCover run failed: " + it.message).let { e -> e.isLogStacktrace = false; e } }
    )

    private suspend fun SequenceScope<CommandLine>.merge(executableFile: Path, tempDirectory: File) {
        val outputSnapshotFile = File(tempDirectory, outputSnapshotFilename)
        if (outputSnapshotFile.isFile && outputSnapshotFile.exists()) {
            _loggerService.writeDebug("The merge command has already been performed for this build step; outputSnapshotFile=${outputSnapshotFile.absolutePath}")
            return
        }

        val snapshotPaths = _dotCoverSettings.additionalSnapshotPaths.map { File(it.path) } + tempDirectory
        val snapshots = collectSnapshots(snapshotPaths)
        if (snapshots.isEmpty()) {
            _loggerService.writeDebug("Snapshot files not found; skipping merge stage")
            return
        }
        if (snapshots.hasSingleSnapshot()) {
            _loggerService.writeDebug("""
                No need to execute merge command: there is a single snapshot file.
                Renaming it: from=${snapshots.first().absolutePath} to=${outputSnapshotFile.absolutePath}
            """.trimIndent())
            FileUtil.rename(snapshots.first(), outputSnapshotFile)
            return
        }

        val configFile = _pathsService.getTempFileName(mergeConfigFilename)
        val virtualConfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
        val sources = snapshots.stream().map { Path(_virtualContext.resolvePath(it.absolutePath)) }.toList()
        val output = Path(_virtualContext.resolvePath(outputSnapshotFile.absolutePath))
        val dotCoverProject = DotCoverProject(DotCoverCommandType.Merge, mergeCommandData = MergeCommandData(sources, output))
        val cliParamsSerializer = selectCommandLineParamsSerializer(_dotCoverAgentTool.type)
        _fileSystemService.write(configFile) {
            cliParamsSerializer.serialize(dotCoverProject, it)
        }

        logSettings("dotCover merge command settings", sources, output)

        yield(
            _dotCoverCommandLineBuilders.get(DotCoverCommandType.Merge)!!.buildCommand(
                executableFile = executableFile,
                environmentVariables = _environmentVariables.getVariables().toList(),
                virtualConfigFilePath.path
            )
        )
        snapshots.forEach { _fileSystemService.remove(it) }
    }

    private suspend fun SequenceScope<CommandLine>.report(executableFile: Path, tempDirectory: File) {
        val reportResultsDirectory = File(tempDirectory, "dotCoverResults")
        _fileSystemService.createDirectory(reportResultsDirectory)
        val outputReportFile = File(reportResultsDirectory, outputReportFilename)

        val outputSnapshotFile = findOutputSnapshot(tempDirectory)
        if (outputSnapshotFile == null) {
            _loggerService.writeWarning("The dotCover report was not generated. Snapshot file not found")
            return
        }
        if (outputReportFile.isFile && outputReportFile.exists()) {
            _loggerService.writeDebug("The report command has already been performed for this build step; outputReportFile=${outputReportFile.absolutePath}")
            return
        }

        val configFile = _pathsService.getTempFileName(reportConfigFilename)
        val virtualConfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
        val source = Path(_virtualContext.resolvePath(outputSnapshotFile.absolutePath))
        val output = Path(_virtualContext.resolvePath(outputReportFile.absolutePath))
        val dotCoverProject = DotCoverProject(DotCoverCommandType.Report, reportCommandData = ReportCommandData(source, output))
        val cliParamsSerializer = selectCommandLineParamsSerializer(_dotCoverAgentTool.type)
        _fileSystemService.write(configFile) {
            cliParamsSerializer.serialize(dotCoverProject, it)
        }

        logSettings("dotCover report command settings", listOf(source), output)

        yield(
            _dotCoverCommandLineBuilders.get(DotCoverCommandType.Report)!!.buildCommand(
                executableFile = executableFile,
                environmentVariables = _environmentVariables.getVariables().toList(),
                virtualConfigFilePath.path
            )
        )

        publishDotCoverArtifacts(outputReportFile, outputSnapshotFile, reportResultsDirectory)
    }

    private fun publishDotCoverArtifacts(outputReportFile: File, outputSnapshotFile: File, reportResultsDirectory: File) {
        if (!outputReportFile.isFile || !outputReportFile.exists()) {
            _loggerService.writeDebug("Nothing to publish: the report file doesn't exist; outputReportFile=${outputReportFile.absolutePath}")
            return
        }

        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        val reportZipFile = File(reportResultsDirectory, outputHtmlReportFilename)

        _dotCoverTeamCityReportGenerator.parseStatementCoverage(outputReportFile)?.let {
            _loggerService.writeStandardOutput("DotCover statement coverage was: ${it.covered} of ${it.total} (${it.percent}%)")
        }

        val configParams = _dotCoverSettings.configParameters.toMutableMap()
        val virtualCheckoutDir = _virtualContext.resolvePath(_pathsService.getPath(PathType.Checkout).canonicalPath)
        if (!configParams.containsKey("dotNetCoverage.dotCover.source.mapping") && virtualCheckoutDir.first() != checkoutDirectory.canonicalPath.first()) {
            configParams["dotNetCoverage.dotCover.source.mapping"] = "$virtualCheckoutDir=>${checkoutDirectory.canonicalPath}"
        }
        _dotCoverTeamCityReportGenerator.generateReportHTMLandStats(_dotCoverSettings.buildLogger, configParams,
            checkoutDirectory, outputReportFile, reportZipFile)?.let {
            _dotnetCoverageStatisticsPublisher.publishCoverageStatistics(it)
        }

        val result = DotnetCoverageGenerationResult(outputReportFile, emptyList(), reportZipFile)
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logsFolder = resolvePath(checkoutDirectory, it)
            result.addFileToPublish(CoverageConstants.DOTCOVER_LOGS, logsFolder)
        }
        result.addFileToPublish(DOTCOVER_SNAPSHOT_DCVR, outputSnapshotFile)
        val publishPath = _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM)

        _uploader.processFiles(reportResultsDirectory, publishPath, result)
    }

    private fun collectSnapshots(snapshotPaths: Sequence<File>) : List<File> {
        _loggerService.writeDebug("Searching for snapshots in the following paths: ${snapshotPaths.joinToString(", ") { it.absolutePath }}")
        return snapshotPaths
            .partition { it.isDirectory }
            .let { (directories, files) ->
                files + directories.flatMap { _fileSystemService.list(it).filter { it.extension == DOTCOVER_SNAPSHOT_EXTENSION } }
            }
            .also { _loggerService.writeDebug("Found ${it.size} snapshots") }
    }

    private fun logSettings(blockName: String, sources: List<Path>, output: Path) {
        val message = """
            |Sources:
            |  ${sources.joinToString("\n  ")}
            |Output:
            |  ${output.toString()}""".trimMargin()
        _loggerService.writeTraceBlock(blockName).use {
            _loggerService.writeTrace(message)
        }
    }

    private fun selectCommandLineParamsFileName(toolType: DotCoverToolType) = when (toolType) {
        DotCoverToolType.CrossPlatformV3 -> "dotCover.rsp"
        else -> "dotCover.xml"
    }

    private fun selectCommandLineParamsSerializer(toolType: DotCoverToolType) = when (toolType) {
        DotCoverToolType.CrossPlatformV3 -> _dotCoverResponseFileSerializer
        else -> _dotCoverRunConfigFileSerializer
    }

    private fun findOutputSnapshot(snapshotDirectory: File) =
        _fileSystemService.list(snapshotDirectory)
            .firstOrNull { it.name.startsWith(MERGED_SNAPSHOT_PREFIX) && it.extension == DOTCOVER_SNAPSHOT_EXTENSION }

    private fun List<File>.hasSingleSnapshot(): Boolean = this.size == 1

    private val mergeConfigFilename get() = "merge_${selectCommandLineParamsFileName(_dotCoverAgentTool.type)}"

    private val reportConfigFilename get() = "report_${selectCommandLineParamsFileName(_dotCoverAgentTool.type)}"

    private val outputSnapshotFilename get() = "${MERGED_SNAPSHOT_PREFIX}_${_dotCoverSettings.buildStepId}.dcvr"

    private val outputReportFilename get() = "CoverageReport_${_dotCoverSettings.buildStepId}.xml"

    private val outputHtmlReportFilename get() = "coverage_${_dotCoverSettings.buildStepId}.zip"

    companion object {
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = "dcvr"
        private const val MERGED_SNAPSHOT_PREFIX = "outputSnapshot"
    }
}