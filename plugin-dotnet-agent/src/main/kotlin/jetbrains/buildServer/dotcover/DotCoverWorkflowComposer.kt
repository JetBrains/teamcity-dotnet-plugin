package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.command.*
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class DotCoverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverRunConfigFileSerializer: DotCoverRunConfigFileSerializer,
    private val _dotCoverResponseFileSerializer: DotCoverResponseFileSerializer,
    private val _loggerService: LoggerService,
    private val _argumentsService: ArgumentsService,
    private val _coverageFilterProvider: CoverageFilterProvider,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
    private val _entryPointSelector: DotCoverEntryPointSelector,
    private val _dotCoverSettings: DotCoverSettings,
    private val _dotCoverAgentTool : DotCoverAgentTool,
    dotCoverCommandLineBuildersList: List<DotCoverCommandLineBuilder>
) : SimpleWorkflowComposer {

    private val _dotCoverCommandLineBuilders: Map<DotCoverCommandType, DotCoverCommandLineBuilder> =
        dotCoverCommandLineBuildersList.associateBy { it.type }
    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow {
        if (_dotCoverSettings.dotCoverMode.isDisabled) {
            return workflow
        }
        if (_dotCoverSettings.dotCoverHomePath.isNullOrBlank()) {
            _loggerService.writeWarning("Skip code coverage: dotCover is enabled, but tool home path has not been set")
            return workflow
        }

        val baseCommandLineIterator = workflow.commandLines.iterator()

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

        val commandLineParamsFile = _pathsService.getTempFileName(
            selectCommandLineParamsFileName(_dotCoverAgentTool.type)
        )
        val snapshotFile = _pathsService.getTempFileName(".${DOTCOVER_SNAPSHOT_EXTENSION}")
        val virtualWorkingDirectory = Path(_virtualContext.resolvePath(baseCommandLine.workingDirectory.path))
        val virtualCommandLineParamsFilePath = Path(_virtualContext.resolvePath(commandLineParamsFile.path))
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
                virtualCommandLineParamsFilePath,
                virtualSnapshotFilePath
            )
        )

        val cliParamsSerializer = selectCommandLineParamsSerializer(_dotCoverAgentTool.type)
        _fileSystemService.write(commandLineParamsFile) {
            cliParamsSerializer.serialize(dotCoverProject, it)
        }

        logSettings(baseCommandLine)

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
                    virtualCommandLineParamsFilePath.path,
                    baseCommandLine
                )
            )
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

    private fun logSettings(commandLine: CommandLine) {
        _loggerService.writeTraceBlock("dotCover settings").use {
            val args = _argumentsService.combine(commandLine.arguments.map { it.value }.asSequence())
            _loggerService.writeTrace("Command line:")
            _loggerService.writeTrace("  \"${commandLine.executableFile.path}\" $args")

            _loggerService.writeTrace("Filters:")
            for (filter in _coverageFilterProvider.filters) {
                _loggerService.writeTrace("  $filter")
            }

            _loggerService.writeTrace("Attribute Filters:")
            for (filter in _coverageFilterProvider.attributeFilters) {
                _loggerService.writeTrace("  $filter")
            }
        }
    }

    private fun overrideDotCoverHome() =
        _loggerService.writeMessage(DotCoverServiceMessage(Path(_dotCoverSettings.dotCoverHomePath)))

    companion object {
        internal const val DOTCOVER_DATA_PROCESSOR_TYPE = CoverageConstants.COVERAGE_TYPE
        internal const val DOTCOVER_TOOL_NAME = "dotcover"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = "dcvr"
    }
}