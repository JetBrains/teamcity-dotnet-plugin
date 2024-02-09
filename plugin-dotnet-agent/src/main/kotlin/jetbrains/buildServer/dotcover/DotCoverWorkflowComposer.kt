package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.command.*
import jetbrains.buildServer.dotcover.report.DotCoverTeamCityReportGenerator
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.ArtifactsUploader
import jetbrains.buildServer.dotcover.statistics.DotnetCoverageStatisticsPublisher
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_SNAPSHOT_DCVR
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGenerationResult
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.FileUtil.resolvePath
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
    private val _dotCoverSettingsHolder: DotCoverSettingsHolder,
    dotCoverCommandLineBuildersList: List<DotCoverCommandLineBuilder>,
    private val _dotCoverTeamCityReportGenerator: DotCoverTeamCityReportGenerator,
    private val _dotnetCoverageStatisticsPublisher: DotnetCoverageStatisticsPublisher,
    private val _uploader: ArtifactsUploader
) : SimpleWorkflowComposer {

    private val _dotCoverCommandLineBuilders: Map<DotCoverCommandType, DotCoverCommandLineBuilder> =
        dotCoverCommandLineBuildersList.associateBy { it.type }
    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow = when {
        !dotCoverEnabled || dotCoverPath.isBlank() -> workflow

        else -> _entryPointSelector.select()
            .fold(
                onSuccess = { Workflow(createDotCoverCommandLine(workflow, context, it.path)) },
                onFailure = { when {
                    it is ToolCannotBeFoundException -> {
                        throw RunBuildException("dotCover run failed: " + it.message)
                            .let { e -> e.isLogStacktrace = false; e }
                    }
                    else -> workflow
                }}
            )
    }

    private fun createDotCoverCommandLine(
        workflow: Workflow,
        context: WorkflowContext,
        entryPointPath: String
    ) = sequence {
        var dotCoverHome = false
        val executableFile = Path(_virtualContext.resolvePath(entryPointPath))
        val virtualTempDirectory = File(_virtualContext.resolvePath(_pathsService.getPath(PathType.AgentTemp).canonicalPath))

        for (baseCommandLine in workflow.commandLines) {
            if (!baseCommandLine.chain.any { it.target == TargetType.Tool }) {
                yield(baseCommandLine)
                continue
            }

            val configFile = _pathsService.getTempFileName(DOTCOVER_CONFIG_EXTENSION)
            val snapshotFile = _pathsService.getTempFileName(DOTCOVER_SNAPSHOT_EXTENSION)
            val virtualWorkingDirectory = Path(_virtualContext.resolvePath(baseCommandLine.workingDirectory.path))
            val virtualConfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
            val virtualSnapshotFilePath = Path(_virtualContext.resolvePath(snapshotFile.path))

            val dotCoverProject = DotCoverProject(
                DotCoverCommandType.Cover,
                CoverCommandData(
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
                    // Overrides the dotCover home path once
                    if (!dotCoverHome) {
                        _loggerService.writeMessage(DotCoverServiceMessage(Path(dotCoverPath)))
                        dotCoverHome = true
                    }

                    if (_dotCoverSettingsHolder.coveragePostProcessingEnabled) {
                        // The snapshot path should be virtual because of the docker wrapper converts it back
                        _loggerService.importData(DOTCOVER_DATA_PROCESSOR_TYPE, virtualSnapshotFilePath, DOTCOVER_TOOL_NAME)
                    }
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

        if (_dotCoverSettingsHolder.coveragePostProcessingEnabled) {
            return@sequence
        }

        merge(executableFile, virtualTempDirectory)
        report(executableFile, virtualTempDirectory)
    }

    private suspend fun SequenceScope<CommandLine>.merge(executableFile: Path, virtualTempDirectory: File) {
        val outputSnapshotFile = File(_virtualContext.resolvePath(File(virtualTempDirectory, outputSnapshotFilename).canonicalPath))

        if (!_dotCoverSettingsHolder.mergeSnapshots) {
            return
        }
        if (outputSnapshotFile.isFile && outputSnapshotFile.exists()) {
            return
        }

        val snapshots = collectSnapshots(virtualTempDirectory)
        if (snapshots.isEmpty()) {
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
        val outputSnapshotFile = findOutputSnapshot(virtualTempDirectory) ?: return

        if (!_dotCoverSettingsHolder.makeReport) {
            return
        }
        if (outputReportFile.isFile && outputReportFile.exists()) {
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

        if (outputReportFile.isFile && outputReportFile.exists()) {
            publishDotCoverArtifacts(outputReportFile, outputSnapshotFile, virtualReportResultsDirectory)
        }
    }

    private fun publishDotCoverArtifacts(outputReportFile: File, outputSnapshotFile: File, virtualReportResultsDirectory: File) {
        val virtualCheckoutDirectory = File(_virtualContext.resolvePath(_pathsService.getPath(PathType.Checkout).canonicalPath))
        val reportZipFile = File(_virtualContext.resolvePath(File(virtualReportResultsDirectory, outputHtmlReportFilename).canonicalPath))

        _dotCoverTeamCityReportGenerator.parseStatementCoverage(outputReportFile)?.let {
            _loggerService.writeStandardOutput("DotCover statement coverage was: ${it.covered} of ${it.total} (${it.percent}%)")
        }
        val coverageStatistics = _dotCoverTeamCityReportGenerator.generateReportHTMLandStats(_dotCoverSettingsHolder.buildLogger, _dotCoverSettingsHolder.configParameters,
            virtualCheckoutDirectory, outputReportFile, reportZipFile)
        coverageStatistics?.let {
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

    private fun collectSnapshots(vararg snapshotPaths: File): List<File> {
        val result = ArrayList<File>()
        for (snapshotPath in snapshotPaths) {
            _fileSystemService.list(snapshotPath)
                .filter { it.extension == "dcvr" }
                .forEach { result.add(it) }
        }
        return result
    }

    private fun findOutputSnapshot(snapshotPath: File): File? {
        return _fileSystemService.list(snapshotPath)
            .filter { it.extension == "dcvr" && it.name.startsWith("outputSnapshot") }
            .firstOrNull()
    }

    private val dotCoverEnabled
        get(): Boolean {
            _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)?.let {
                if (it == CoverageConstants.PARAM_DOTCOVER) return true
            }

            _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled")?.let {
                if (it.trim().toBoolean()) return true
            }

            return false
        }

    private val dotCoverPath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    private val mergeConfigFilename get() = "merge_$DOTCOVER_CONFIG_EXTENSION"

    private val reportConfigFilename get() = "report_$DOTCOVER_CONFIG_EXTENSION"

    private val outputSnapshotFilename get() = "outputSnapshot_${_dotCoverSettingsHolder.buildStepId}.dcvr"

    private val outputReportFilename get() = "CoverageReport_${_dotCoverSettingsHolder.buildStepId}.xml"

    private val outputHtmlReportFilename get() = "coverage_${_dotCoverSettingsHolder.buildStepId}.zip"

    companion object {
        internal const val DOTCOVER_DATA_PROCESSOR_TYPE = CoverageConstants.COVERAGE_TYPE
        internal const val DOTCOVER_TOOL_NAME = "dotcover"
        internal const val DOTCOVER_CONFIG_EXTENSION = "dotCover.xml"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = ".dcvr"
    }
}