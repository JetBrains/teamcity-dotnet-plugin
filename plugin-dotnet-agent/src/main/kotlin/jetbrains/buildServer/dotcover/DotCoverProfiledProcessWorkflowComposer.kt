package jetbrains.buildServer.dotcover

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class DotCoverProfiledProcessWorkflowComposer(
    private val _buildInfo: BuildInfo,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _argumentsService: ArgumentsService,
    private val _targetService: TargetService,
    private val _buildOptions: BuildOptions,
    private val _loggerService: LoggerService,
) : SimpleWorkflowComposer {
    override val target = TargetType.Tool

    private val supportedRunnerTypes = listOf(CoverageConstants.DOTCOVER_RUNNER_TYPE)

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) = when {
        supportedRunnerTypes.contains(_buildInfo.runType) -> sequence {
            commandLineParameter
                ?.let { createCommandLines(it, context) }
                ?.let { yield(it) }
        }.let(::Workflow)

        else -> Workflow()
    }

    private fun createCommandLines(commandLineString: String, context: WorkflowContext) : CommandLine {
        val (executable, arguments) = commandLineString.trim().split("\\s+".toRegex(), limit = 2)
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
                return CommandLine(
                    baseCommandLine = null,
                    target = target,
                    executableFile = Path(executable),
                    workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                    arguments = args
                )
            }
    }

    private val commandLineParameter get() =
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE)?.trim()
}