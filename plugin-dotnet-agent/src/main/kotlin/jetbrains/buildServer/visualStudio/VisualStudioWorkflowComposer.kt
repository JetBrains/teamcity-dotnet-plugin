package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import java.io.File

class VisualStudioWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _targetService: TargetService,
        private val _toolResolver: ToolResolver,
        private val _targetRegistry: TargetRegistry,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow) =
            Workflow(sequence {
                parameters(DotnetConstants.PARAM_COMMAND)?.let {
                    if (!DotnetCommandType.VisualStudio.id.equals(it, true)) {
                        return@sequence
                    }
                } ?: return@sequence

                val workingDirectory = Path(_virtualContext.resolvePath(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath))
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
                    _argumentsService.split(it).map { CommandLineArgument(it, CommandLineArgumentType.Custom) }.toList()
                } ?: emptyList()

                val executableFile = Path(_virtualContext.resolvePath(_toolResolver.executableFile.canonicalPath))

                for ((targetFile) in _targetService.targets) {
                    disposableOf(
                            // Subscribe for an exit code
                            context.subscribe {
                                when {
                                    it is CommandResultExitCode -> {
                                        if (it.exitCode != 0) {
                                            _loggerService.writeBuildProblem(BuildProblemData.createBuildProblem("visual_studio_exit_code${it.exitCode}", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code ${it.exitCode}"))
                                            context.abort(BuildFinishedStatus.FINISHED_FAILED)
                                        }
                                    }
                                }
                            },
                            // Register the current target
                            _targetRegistry.register(target)
                    ).use {
                        yield(CommandLine(
                                TargetType.Tool,
                                executableFile,
                                workingDirectory,
                                sequence {
                                    yield(CommandLineArgument(_virtualContext.resolvePath(targetFile.canonicalPath), CommandLineArgumentType.Mandatory))
                                    yield(CommandLineArgument("/$action", CommandLineArgumentType.Mandatory))
                                    if (!configValue.isBlank()) {
                                        yield(CommandLineArgument(configValue))
                                    }

                                    yieldAll(args)
                                }.toList(),
                                emptyList()))
                    }


                    if (context.status != WorkflowStatus.Running) {
                        return@sequence
                    }
                }
            })

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}