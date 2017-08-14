package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotnet.arguments.*
import jetbrains.buildServer.runners.*
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetArgumentsProvider: ArgumentsProvider) : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        if(workflow.commandLines.any()) {
            throw RunBuildException("This composer should be a root")
        }

        val toolPath: File
        try {
            toolPath = _pathsService.getToolPath(DotnetConstants.RUNNER_TYPE)
        }
        catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        return Workflow(
                buildSequence {
                    yield(
                            CommandLine(
                                TargetType.Tool,
                                toolPath,
                                _pathsService.getPath(PathType.WorkingDirectory),
                                    _dotnetArgumentsProvider.getArguments(),
                                _defaultEnvironmentVariables.variables))

                    if(!context.lastResult.isCompleted) {
                        return@buildSequence
                    }

                    // var exitCode = context.lastResult.exitCode
                    // var standardOutput = context.lastResult.standardOutput.toList()
                    // var errorOutput = context.lastResult.errorOutput.toList()
                }
        )
    }
}