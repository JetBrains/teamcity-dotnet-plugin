

package jetbrains.buildServer

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.ToolState
import jetbrains.buildServer.dotnet.ToolStateWorkflowComposer
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.util.OSType
import java.io.File

class ExecutableWorkflowComposer(
    private val _dotnetToolResolver: DotnetToolResolver,
    private val _dotnetStateWorkflowComposer: ToolStateWorkflowComposer,
    private val _virtualContext: VirtualContext,
    private val _cannotExecute: CannotExecute
) : SimpleWorkflowComposer {

    override val target = TargetType.ToolHost

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
        Workflow(sequence {
            var dotnetExecutableFile: String? = null
            for (commandLine in workflow.commandLines) {
                if (context.status != WorkflowStatus.Running) {
                    break
                }

                val executableFile = commandLine.executableFile.path
                val executableFileExtension =
                    if (executableFile.isNotBlank()) File(executableFile).extension.trim().lowercase()
                    else ""

                when {
                    // native windows
                    "exe".equals(executableFileExtension, true) || "com".equals(executableFileExtension, true) -> {
                        if (_virtualContext.targetOSType != OSType.WINDOWS) {
                            _cannotExecute.writeBuildProblemFor(commandLine.executableFile)
                        } else yield(commandLine)
                    }

                    // dotnet host
                    executableFile.isBlank() || "dll".equals(executableFileExtension, true) -> {
                        val cmdArgs = when {
                            executableFile.isNotBlank() ->
                                listOf(CommandLineArgument(executableFile, CommandLineArgumentType.Target)) + commandLine.arguments

                            else -> commandLine.arguments
                        }

                        val description: List<StdOutText> = commandLine.description
                        val defaultDotnetExecutableFile = _dotnetToolResolver.executable
                        if (_virtualContext.isVirtual && dotnetExecutableFile == null) {
                            val toolState = ToolState(
                                defaultDotnetExecutableFile,
                                observer { dotnetExecutableFile = it.path }
                            )

                            yieldAll(_dotnetStateWorkflowComposer.compose(context, toolState).commandLines)
                        }

                        yield(CommandLine(
                            commandLine,
                            commandLine.target,
                            Path(dotnetExecutableFile ?: defaultDotnetExecutableFile.path.path),
                            commandLine.workingDirectory,
                            cmdArgs,
                            commandLine.environmentVariables,
                            commandLine.title,
                            description
                        ))
                    }
                    else -> {
                        yield(commandLine)
                    }
                }
            }
        })
}