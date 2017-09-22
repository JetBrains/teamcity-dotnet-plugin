package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_ARGUMENTS
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_ACTION
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_CONFIG
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_PLATFORM
import jetbrains.buildServer.dotnet.TargetService
import java.io.File
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class VisualStudioWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _targetService: TargetService,
        private val _toolResolver: ToolResolver)
    : WorkflowComposer {
    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            Workflow(
                    buildSequence {
                        parameters(DotnetConstants.PARAM_COMMAND)?.let {
                            if (!DotnetCommandType.VisualStudio.id.equals(it, true)) {
                                return@buildSequence
                            }
                        } ?: return@buildSequence

                        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
                        val action = parameters(PARAM_VISUAL_STUDIO_ACTION) ?: throw RunBuildException("Parameter \"$PARAM_VISUAL_STUDIO_ACTION\" was not found")

                        val configItems = listOf(parameters(PARAM_VISUAL_STUDIO_CONFIG, ""), parameters(PARAM_VISUAL_STUDIO_PLATFORM, "")).filter { !it.isNullOrBlank() }
                        var configValue = configItems.joinToString("|")
                        if (configItems.size > 1) {
                            configValue = "\"$configValue\""
                        }

                        val args = parameters(PARAM_ARGUMENTS)?.trim()?.let {
                            _argumentsService.split(it).map { CommandLineArgument(it) }.toList()
                        } ?: emptyList()

                        val executableFile = _toolResolver.executableFile

                        for (commandTarget in _targetService.targets) {
                            yield(CommandLine(
                                    TargetType.Tool,
                                    executableFile,
                                    checkoutDirectory,
                                    buildSequence {
                                        yield(CommandLineArgument(commandTarget.targetFile.absolutePath))
                                        yield(CommandLineArgument("/$action"))
                                        if (!configValue.isNullOrBlank()) {
                                            yield(CommandLineArgument(configValue))
                                        }

                                        yieldAll(args)
                                    }.toList(),
                                    emptyList<CommandLineEnvironmentVariable>()))

                            if (context.lastResult.isCompleted && context.lastResult.exitCode != 0) {
                                context.abort(BuildFinishedStatus.FINISHED_FAILED)
                                return@buildSequence
                            }
                        }
                }
            )

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}