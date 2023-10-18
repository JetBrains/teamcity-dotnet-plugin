package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType
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
) : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow {
        if (!dotCoverEnabled || dotCoverPath.isBlank()) {
            return workflow
        }

        try {
            return _entryPointSelector.select()
                .map { Workflow(createDotCoverCommandLine(workflow, context, it.path)) }
                .getOrDefault(workflow)
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException("dotCover run failed: " + e.message)
            exception.isLogStacktrace = false
            throw exception
        }
    }

    private fun createDotCoverCommandLine(
        workflow: Workflow,
        context: WorkflowContext,
        entryPointPath: String
    ) = sequence {
        var dotCoverHome = false
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

                    // The snapshot path should be virtual because of the docker wrapper converts it back
                    _loggerService.importData(DOTCOVER_DATA_PROCESSOR_TYPE, virtualSnapshotFilePath, DOTCOVER_TOOL_NAME)
                }
            }.use {
                yield(
                    CommandLine(
                        baseCommandLine = baseCommandLine,
                        target = target,
                        executableFile = Path(_virtualContext.resolvePath(entryPointPath)),
                        workingDirectory = baseCommandLine.workingDirectory,
                        arguments = createArguments(dotCoverProject).toList(),
                        environmentVariables = baseCommandLine.environmentVariables + _environmentVariables.getVariables(),
                        title = baseCommandLine.title
                    )
                )
            }
        }
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

    private fun createArguments(dotCoverProject: DotCoverProject) = sequence {
        yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(dotCoverProject.configFile.path, CommandLineArgumentType.Target))
        yield(CommandLineArgument("${argumentPrefix}ReturnTargetExitCode"))
        yield(CommandLineArgument("${argumentPrefix}AnalyzeTargetArguments=false"))

        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(File(it), "dotCover", ".log").canonicalPath)
            yield(CommandLineArgument("${argumentPrefix}LogFile=${logFileName}", CommandLineArgumentType.Infrastructural))
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }

    private val argumentPrefix get () = when(_virtualContext.targetOSType) {
        OSType.WINDOWS -> "/"
        else -> "--"
    }

    private val dotCoverPath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            .let { when (it.isNullOrBlank()) {
                true -> ""
                false -> it
            }}

    companion object {
        internal const val DOTCOVER_DATA_PROCESSOR_TYPE = CoverageConstants.COVERAGE_TYPE
        internal const val DOTCOVER_TOOL_NAME = "dotcover"
        internal const val DOTCOVER_CONFIG_EXTENSION = "dotCover.xml"
        internal const val DOTCOVER_SNAPSHOT_EXTENSION = ".dcvr"
    }
}