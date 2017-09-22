package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetCommandSet: CommandSet) : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        @Suppress("EXPERIMENTAL_FEATURE_WARNING")
        return Workflow(
                buildSequence {
                    for (command in _dotnetCommandSet.commands) {
                        yield(
                                CommandLine(
                                        TargetType.Tool,
                                        command.toolResolver.executableFile,
                                        _pathsService.getPath(PathType.WorkingDirectory),
                                        command.arguments.toList(),
                                        _defaultEnvironmentVariables.variables.toList()))

                        if (context.lastResult.isCompleted && !command.isSuccessfulExitCode(context.lastResult.exitCode)) {
                            context.abort(BuildFinishedStatus.FINISHED_FAILED)
                            return@buildSequence
                        }
                    }
                }
        )
    }
}