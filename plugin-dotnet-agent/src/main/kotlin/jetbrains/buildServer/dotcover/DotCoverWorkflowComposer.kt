package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProject.*
import jetbrains.buildServer.dotcover.command.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class DotCoverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
    private val _loggerService: LoggerService,
    private val _argumentsService: ArgumentsService,
    private val _coverageFilterProvider: CoverageFilterProvider,
    private val _virtualContext: VirtualContext,
    private val _environmentVariables: EnvironmentVariables,
    private val _entryPointSelector: DotCoverEntryPointSelector,
    private val _dotCoverSettings: DotCoverSettings,
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
            _loggerService.writeWarning("Skip code coverage: dotCover is enabled however tool home path has not been set")
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

        val configFile = _pathsService.getTempFileName(DOTCOVER_CONFIG_EXTENSION)
        val snapshotFile = _pathsService.getTempFileName(".${DOTCOVER_SNAPSHOT_EXTENSION}")
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

    private fun overrideDotCoverHome() =
        _loggerService.writeMessage(DotCoverServiceMessage(Path(_dotCoverSettings.dotCoverHomePath)))

    companion object {
        internal const val DOTCOVER_DATA_PROCESSOR_TYPE = CoverageConstants.COVERAGE_TYPE
        internal const val DOTCOVER_TOOL_NAME = "dotcover"
        internal const val DOTCOVER_CONFIG_EXTENSION = "dotCover.xml"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = "dcvr"
    }
}