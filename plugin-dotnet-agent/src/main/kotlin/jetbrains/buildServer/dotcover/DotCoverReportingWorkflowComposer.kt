package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.command.*
import jetbrains.buildServer.dotcover.report.DotCoverTeamCityReportGenerator
import jetbrains.buildServer.dotcover.statistics.DotnetCoverageStatisticsPublisher
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_SNAPSHOT_DCVR
import jetbrains.buildServer.dotnet.coverage.ArtifactsUploader
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGenerationResult
import jetbrains.buildServer.util.FileUtil.resolvePath
import jetbrains.buildServer.util.FileUtil
import java.io.File

class DotCoverReportingWorkflowComposer(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
    private val _loggerService: LoggerService,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
    private val _entryPointSelector: DotCoverEntryPointSelector,
    private val _dotCoverSettings: DotCoverSettings,
    dotCoverCommandLineBuildersList: List<DotCoverCommandLineBuilder>,
    private val _dotCoverTeamCityReportGenerator: DotCoverTeamCityReportGenerator,
    private val _dotnetCoverageStatisticsPublisher: DotnetCoverageStatisticsPublisher,
    private val _uploader: ArtifactsUploader
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
            val virtualTempDirectory = File(_virtualContext.resolvePath(_pathsService.getPath(PathType.AgentTemp).canonicalPath))

            // merge
            _dotCoverSettings.shouldMergeSnapshots().let { (shouldMergeSnapshots, logMessage) -> when {
                shouldMergeSnapshots -> merge(executablePath, virtualTempDirectory)
                else -> _loggerService.writeDebug(logMessage)
            }}

            // report
            _dotCoverSettings.shouldGenerateReport().let { (shouldGenerateReport, logMessage) -> when {
                shouldGenerateReport -> report(executablePath, virtualTempDirectory)
                else -> _loggerService.writeDebug(logMessage)
            }}
        }.let(::Workflow)
    }

    private fun getDotCoverExecutablePath(): Path = _entryPointSelector.select().fold(
        onSuccess = { return Path(_virtualContext.resolvePath(it.path)) },
        onFailure = { throw RunBuildException("dotCover run failed: " + it.message).let { e -> e.isLogStacktrace = false; e } }
    )

    private suspend fun SequenceScope<CommandLine>.merge(executableFile: Path, virtualTempDirectory: File) {
        val outputSnapshotFile = File(_virtualContext.resolvePath(File(virtualTempDirectory, outputSnapshotFilename).canonicalPath))
        if (outputSnapshotFile.isFile && outputSnapshotFile.exists()) {
            _loggerService.writeDebug("The merge command has already been performed for this build step; outputSnapshotFile=${outputSnapshotFile.absolutePath}")
            return
        }

        val snapshotPaths = _dotCoverSettings.additionalSnapshotPaths.map { File(it.path) } + virtualTempDirectory
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

        val virtualConfigFilePath = Path(_virtualContext.resolvePath(_pathsService.getTempFileName(mergeConfigFilename).path))
        val dotCoverProject = DotCoverProject(DotCoverCommandType.Merge, mergeCommandData = MergeCommandData(snapshots, outputSnapshotFile))
        _fileSystemService.write(File(virtualConfigFilePath.path)) {
            _dotCoverProjectSerializer.serialize(dotCoverProject, it)
        }

        yield(
            _dotCoverCommandLineBuilders.get(DotCoverCommandType.Merge)!!.buildCommand(
                executableFile = executableFile,
                environmentVariables = _environmentVariables.getVariables().toList(),
                virtualConfigFilePath.path
            )
        )
        snapshots.forEach { _fileSystemService.remove(it) }
    }

    private suspend fun SequenceScope<CommandLine>.report(executableFile: Path, virtualTempDirectory: File) {
        val virtualReportResultsDirectory = File(_virtualContext.resolvePath(File(virtualTempDirectory, "dotCoverResults").canonicalPath))
        val outputReportFile = File(_virtualContext.resolvePath(File(virtualReportResultsDirectory, outputReportFilename).canonicalPath))

        val outputSnapshotFile = findOutputSnapshot(virtualTempDirectory)
        if (outputSnapshotFile == null) {
            _loggerService.writeWarning("The dotCover report was not generated. Snapshot file not found")
            return
        }
        if (outputReportFile.isFile && outputReportFile.exists()) {
            _loggerService.writeDebug("The report command has already been performed for this build step; outputReportFile=${outputReportFile.absolutePath}")
            return
        }

        val virtualConfigFilePath = Path(_virtualContext.resolvePath(_pathsService.getTempFileName(reportConfigFilename).path))
        val dotCoverProject = DotCoverProject(DotCoverCommandType.Report, reportCommandData = ReportCommandData(outputSnapshotFile, outputReportFile))
        _fileSystemService.write(File(virtualConfigFilePath.path)) {
            _dotCoverProjectSerializer.serialize(dotCoverProject, it)
        }

        yield(
            _dotCoverCommandLineBuilders.get(DotCoverCommandType.Report)!!.buildCommand(
                executableFile = executableFile,
                environmentVariables = _environmentVariables.getVariables().toList(),
                virtualConfigFilePath.path
            )
        )

        publishDotCoverArtifacts(outputReportFile, outputSnapshotFile, virtualReportResultsDirectory)
    }

    private fun publishDotCoverArtifacts(outputReportFile: File, outputSnapshotFile: File, virtualReportResultsDirectory: File) {
        if (!outputReportFile.isFile || !outputReportFile.exists()) {
            _loggerService.writeDebug("Nothing to publish: the report file doesn't exist; outputReportFile=${outputReportFile.absolutePath}")
            return
        }

        val virtualCheckoutDirectory = File(_virtualContext.resolvePath(_pathsService.getPath(PathType.Checkout).canonicalPath))
        val reportZipFile = File(_virtualContext.resolvePath(File(virtualReportResultsDirectory, outputHtmlReportFilename).canonicalPath))

        _dotCoverTeamCityReportGenerator.parseStatementCoverage(outputReportFile)?.let {
            _loggerService.writeStandardOutput("DotCover statement coverage was: ${it.covered} of ${it.total} (${it.percent}%)")
        }
        _dotCoverTeamCityReportGenerator.generateReportHTMLandStats(_dotCoverSettings.buildLogger, _dotCoverSettings.configParameters,
            virtualCheckoutDirectory, outputReportFile, reportZipFile)?.let {
            _dotnetCoverageStatisticsPublisher.publishCoverageStatistics(it)
        }

        val result = DotnetCoverageGenerationResult(outputReportFile, emptyList(), reportZipFile)
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logsFolder = resolvePath(virtualCheckoutDirectory, it)
            result.addFileToPublish(CoverageConstants.DOTCOVER_LOGS, logsFolder)
        }
        result.addFileToPublish(DOTCOVER_SNAPSHOT_DCVR, outputSnapshotFile)
        val publishPath = _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM)

        _uploader.processFiles(virtualReportResultsDirectory, publishPath, result)
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

    private fun findOutputSnapshot(snapshotDirectory: File) =
        _fileSystemService.list(snapshotDirectory)
            .firstOrNull { it.name.startsWith(MERGED_SNAPSHOT_PREFIX) && it.extension == DOTCOVER_SNAPSHOT_EXTENSION }

    private fun List<File>.hasSingleSnapshot(): Boolean = this.size == 1

    private val mergeConfigFilename get() = "merge_$DOTCOVER_CONFIG_EXTENSION"

    private val reportConfigFilename get() = "report_$DOTCOVER_CONFIG_EXTENSION"

    private val outputSnapshotFilename get() = "${MERGED_SNAPSHOT_PREFIX}_${_dotCoverSettings.buildStepId}.dcvr"

    private val outputReportFilename get() = "CoverageReport_${_dotCoverSettings.buildStepId}.xml"

    private val outputHtmlReportFilename get() = "coverage_${_dotCoverSettings.buildStepId}.zip"

    companion object {
        internal const val DOTCOVER_CONFIG_EXTENSION = "dotCover.xml"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = "dcvr"
        private const val MERGED_SNAPSHOT_PREFIX = "outputSnapshot"
    }
}