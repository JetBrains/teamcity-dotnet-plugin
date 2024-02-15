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
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.FileUtil.resolvePath
import jetbrains.buildServer.util.FileUtil
import java.io.File

class DotCoverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
    private val _loggerService: LoggerService,
    private val _argumentsService: ArgumentsService,
    private val _coverageFilterProvider: CoverageFilterProvider,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
    private val _entryPointSelector: DotCoverEntryPointSelector,
    private val _dotCoverSettings: DotCoverSettings,
    dotCoverCommandLineBuildersList: List<DotCoverCommandLineBuilder>,
    private val _dotCoverTeamCityReportGenerator: DotCoverTeamCityReportGenerator,
    private val _dotnetCoverageStatisticsPublisher: DotnetCoverageStatisticsPublisher,
    private val _uploader: ArtifactsUploader
) : SimpleWorkflowComposer {

    private val _dotCoverCommandLineBuilders: Map<DotCoverCommandType, DotCoverCommandLineBuilder> =
        dotCoverCommandLineBuildersList.associateBy { it.type }
    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow {
        if (_dotCoverSettings.dotCoverMode.isDisabled) {
            return workflow
        }
        if (_dotCoverSettings.dotCoverHomePath.isNullOrBlank()) {
            _loggerService.writeWarning("Skip code coverage: dotCover is enabled however tool home path has not been set")
            return workflow
        }

        val baseCommandLineIterator = workflow.commandLines.iterator()
//        if (!baseCommandLineIterator.hasNext()) {
//            return workflow
//        }

        // TODO should not be applied in case absence of the base command line expect case of having snaphosts

        return sequence {
            val executablePath = getDotCoverExecutablePath()

            // cover applicable base command line
            var dotCoverHomeOverriden = false
            while (baseCommandLineIterator.hasNext()) {
                val baseCommandLine = baseCommandLineIterator.next()
                if (!baseCommandLine.chain.any { it.target == TargetType.Tool }) {
                    yield(baseCommandLine)
                    continue
                }
                cover(baseCommandLine, context, executablePath, onSuccess = {
                    if (!dotCoverHomeOverriden) {
                        overrideDotCoverHome()
                        dotCoverHomeOverriden = true
                    }
                })
            }

            // fallback for backward compatibility
            if (_dotCoverSettings.coveragePostProcessingEnabled) {
                _loggerService.writeDebug(
                    "Coverage post-processing is enabled; " +
                            "the results will be processed before the build finishes"
                )
                return@sequence
            }

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

    private suspend fun SequenceScope<CommandLine>.cover(
        baseCommandLine: CommandLine,
        context: WorkflowContext,
        executableFile: Path,
        onSuccess: () -> Unit,
    ) {
        if (!baseCommandLine.chain.any { it.target == TargetType.Tool }) {
            yield(baseCommandLine)
            return
        }

        val configFile = _pathsService.getTempFileName(DOTCOVER_CONFIG_EXTENSION)
        val snapshotFile = _pathsService.getTempFileName(DOTCOVER_SNAPSHOT_EXTENSION)
        val virtualWorkingDirectory = Path(_virtualContext.resolvePath(baseCommandLine.workingDirectory.path))
        val virtualConfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
        val virtualSnapshotFilePath = Path(_virtualContext.resolvePath(snapshotFile.path))

        val dotCoverProject = DotCoverProject(
            dotCoverCommandType = DotCoverCommandType.Cover,
            coverCommandData = CoverCommandData(
                CommandLine(
                    baseCommandLine,
                    baseCommandLine.target,
                    baseCommandLine.executableFile,
                    virtualWorkingDirectory,
                    baseCommandLine.arguments,
                    baseCommandLine.environmentVariables,
                    baseCommandLine.title,
                    baseCommandLine.description
                ),
                virtualConfigFilePath,
                virtualSnapshotFilePath
            )
        )

        _fileSystemService.write(configFile) {
            _dotCoverProjectSerializer.serialize(dotCoverProject, it)
        }

        _loggerService.writeTraceBlock("dotCover settings").use {
            val args = _argumentsService.combine(baseCommandLine.arguments.map { it.value }.asSequence())
            _loggerService.writeTrace("Command line:")
            _loggerService.writeTrace("  \"${baseCommandLine.executableFile.path}\" $args")

            _loggerService.writeTrace("Filters:")
            for (filter in _coverageFilterProvider.filters) {
                _loggerService.writeTrace("  $filter")
            }

            _loggerService.writeTrace("Attribute Filters:")
            for (filter in _coverageFilterProvider.attributeFilters) {
                _loggerService.writeTrace("  $filter")
            }
        }

        context.toExitCodes().subscribe {
            if (_fileSystemService.isExists(snapshotFile)) {
                onSuccess()

                if (_dotCoverSettings.coveragePostProcessingEnabled) {
                    // The snapshot path should be virtual because of the docker wrapper converts it back
                    _loggerService.importData(DOTCOVER_DATA_PROCESSOR_TYPE, virtualSnapshotFilePath, DOTCOVER_TOOL_NAME)
                }
                _loggerService.writeStandardOutput("dotCover snapshot file has been produced: ${snapshotFile.absolutePath}")
            }
        }.use {
            yield(
                _dotCoverCommandLineBuilders.get(DotCoverCommandType.Cover)!!.buildCommand(
                    executableFile = executableFile,
                    environmentVariables = baseCommandLine.environmentVariables + _environmentVariables.getVariables(),
                    virtualConfigFilePath.path,
                    baseCommandLine
                )
            )
        }
    }

    private suspend fun SequenceScope<CommandLine>.merge(executableFile: Path, virtualTempDirectory: File) {
        val outputSnapshotFile = File(_virtualContext.resolvePath(File(virtualTempDirectory, outputSnapshotFilename).canonicalPath))
        if (outputSnapshotFile.isFile && outputSnapshotFile.exists()) {
            _loggerService.writeDebug("The merge command has already been performed for this build step; outputSnapshotFile=${outputSnapshotFile.absolutePath}")
            return
        }

        val snapshotPaths = _dotCoverSettings.additionalSnapshotPaths.map { File(it) }.asSequence() + virtualTempDirectory
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

        // TODO must be activated only in case of having snapshots
        if (outputSnapshotFile == null) {
            _loggerService.writeDebug("The report could not be built: a snapshot file is not found. A merge command has to be executed first")
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
                files + directories.flatMap { _fileSystemService.list(it).filter { it.extension == "dcvr" } }
            }
            .also { _loggerService.writeDebug("Found ${it.size} snapshots") }
    }

    private fun findOutputSnapshot(snapshotDirectory: File): File? {
        val allSnapshots = _fileSystemService.list(snapshotDirectory).filter { it.extension == "dcvr" }.toList()
        return if (allSnapshots.size == 1) allSnapshots[0]
        else allSnapshots
            .filter { it.name.startsWith("outputSnapshot") }
            .firstOrNull()
    }

    private fun List<File>.hasSingleSnapshot(): Boolean = this.size == 1

    private fun overrideDotCoverHome() =
        _loggerService.writeMessage(DotCoverServiceMessage(Path(_dotCoverSettings.dotCoverHomePath)))

    private val mergeConfigFilename get() = "merge_$DOTCOVER_CONFIG_EXTENSION"

    private val reportConfigFilename get() = "report_$DOTCOVER_CONFIG_EXTENSION"

    private val outputSnapshotFilename get() = "outputSnapshot_${_dotCoverSettings.buildStepId}.dcvr"

    private val outputReportFilename get() = "CoverageReport_${_dotCoverSettings.buildStepId}.xml"

    private val outputHtmlReportFilename get() = "coverage_${_dotCoverSettings.buildStepId}.zip"

    companion object {
        internal const val DOTCOVER_DATA_PROCESSOR_TYPE = CoverageConstants.COVERAGE_TYPE
        internal const val DOTCOVER_TOOL_NAME = "dotcover"
        internal const val DOTCOVER_CONFIG_EXTENSION = "dotCover.xml"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = ".dcvr"
    }
}