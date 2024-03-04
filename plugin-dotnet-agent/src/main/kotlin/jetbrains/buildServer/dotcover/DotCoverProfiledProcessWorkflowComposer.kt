package jetbrains.buildServer.dotcover

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class DotCoverProfiledProcessWorkflowComposer(
    private val _buildInfo: BuildInfo,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _argumentsService: ArgumentsService,
    private val _buildOptions: BuildOptions,
    private val _loggerService: LoggerService,
    private val _virtualContext: VirtualContext,
) : SimpleWorkflowComposer {
    override val target = TargetType.Tool

    private val supportedRunnerTypes = listOf(CoverageConstants.DOTCOVER_RUNNER_TYPE)

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) = when {
        supportedRunnerTypes.contains(_buildInfo.runType) ->
            coveredProcessExecutablePath
                ?.let { when {
                    it.isBlank() -> null
                    else -> Workflow(createCommandLines(it, coveredProcessArguments, context))
                }}
                ?: Workflow()

        else -> Workflow()
    }

    private fun createCommandLines(executablePath: String, arguments: String, context: WorkflowContext) = sequence<CommandLine> {
        val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)

        val args = arguments.trim().let { argString ->
            _argumentsService.split(argString).map {
                CommandLineArgument(it, CommandLineArgumentType.Custom)
            }.toList()
        }

        context.toExitCodes()
            .subscribe {
                if (it != 0 && _buildOptions.failBuildOnExitCode) {
                    _loggerService.writeBuildProblem(
                        "dotcover_cover_custom_process_exit_code$it",
                        BuildProblemData.TC_EXIT_CODE_TYPE,
                        "Process exited with code $it"
                    )
                    context.abort(BuildFinishedStatus.FINISHED_FAILED)
                }
            }
            .use {
                yield(CommandLine(
                    baseCommandLine = null,
                    target = target,
                    executableFile = Path(_virtualContext.resolvePath(executablePath)),
                    workingDirectory = Path(workingDirectory.path),
                    arguments = args
                ))
            }
    }

    private val coveredProcessExecutablePath get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE)?.trim()

    private val coveredProcessArguments get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_ARGUMENTS)?.trim() ?: ""
}