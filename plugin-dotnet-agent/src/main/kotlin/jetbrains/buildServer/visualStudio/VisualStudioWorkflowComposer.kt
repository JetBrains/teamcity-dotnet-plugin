package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import kotlin.coroutines.experimental.buildSequence

class VisualStudioWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _targetService: TargetService,
        private val _toolResolver: ToolResolver)
    : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow) = Workflow(buildSequence {
        parameters(DotnetConstants.PARAM_COMMAND)?.let {
            if (!DotnetCommandType.VisualStudio.id.equals(it, true)) {
                return@buildSequence
            }
        } ?: return@buildSequence

        val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
        val action = parameters(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION)
                ?: throw RunBuildException("Parameter \"${DotnetConstants.PARAM_VISUAL_STUDIO_ACTION}\" was not found")

        val configItems = listOf(
                parameters(DotnetConstants.PARAM_CONFIG, ""),
                parameters(DotnetConstants.PARAM_PLATFORM, ""))
                .filter { !it.isBlank() }
        var configValue = configItems.joinToString("|")
        if (configItems.size > 1) {
            configValue = "\"$configValue\""
        }

        val args = parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
            _argumentsService.split(it).map { CommandLineArgument(it) }.toList()
        } ?: emptyList()

        val executableFile = _toolResolver.executableFile

        for (commandTarget in _targetService.targets) {
            yield(CommandLine(
                    TargetType.Tool,
                    executableFile,
                    workingDirectory,
                    buildSequence {
                        yield(CommandLineArgument(commandTarget.targetFile.absolutePath))
                        yield(CommandLineArgument("/$action"))
                        if (!configValue.isBlank()) {
                            yield(CommandLineArgument(configValue))
                        }

                        yieldAll(args)
                    }.toList(),
                    emptyList()))

            if (context.lastResult.exitCode != 0) {
                _loggerService.onBuildProblem(BuildProblemData.createBuildProblem("visual_studio_exit_code${context.lastResult.exitCode}", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code ${context.lastResult.exitCode}"))
                context.abort(BuildFinishedStatus.FINISHED_FAILED)
                return@buildSequence
            }
        }
    })

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}