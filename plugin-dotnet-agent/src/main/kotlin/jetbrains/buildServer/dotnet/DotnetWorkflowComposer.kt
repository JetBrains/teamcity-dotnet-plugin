package jetbrains.buildServer.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _failedTestDetector: FailedTestDetector,
        private val _argumentsService: ArgumentsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _vstestLoggerEnvironment: VSTestLoggerEnvironment,
        private val _commandSet: CommandSet) : WorkflowComposer, WorkflowOutputFilter {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        return Workflow(buildSequence {
            context.registerOutputFilter(this@DotnetWorkflowComposer).use {
                for (command in _commandSet.commands) {
                    val targets = command.targetArguments.flatMap { it.arguments }.map { File(it.value) }.toList()
                    _vstestLoggerEnvironment.configure(targets).use {
                        val executableFile = command.toolResolver.executableFile
                        val args = command.arguments.toList()
                        val commandHeader = _argumentsService.combine(sequenceOf(executableFile.name).plus(args.map { it.value }))
                        _loggerService.onStandardOutput(commandHeader)
                        _loggerService.onBlock(command.commandType.id.replace('-', ' ')).use {
                            yield(CommandLine(
                                    TargetType.Tool,
                                    executableFile,
                                    _pathsService.getPath(PathType.WorkingDirectory),
                                    args,
                                    _defaultEnvironmentVariables.variables.toList()))
                        }
                    }

                    if (!command.isSuccessful(context.lastResult)) {
                        _loggerService.onBuildProblem(BuildProblemData.createBuildProblem("dotnet_exit_code${context.lastResult.exitCode}", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code ${context.lastResult.exitCode}"))
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                        return@buildSequence
                    }
                }
            }
        })
    }

    override fun acceptStandardOutput(text: String): Boolean {
        return _failedTestDetector.hasFailedTest(text)
    }

    override fun acceptErrorOutput(text: String): Boolean {
        return false
    }
}