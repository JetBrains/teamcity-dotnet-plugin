

package jetbrains.buildServer.cmd

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class CmdWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext,
        private val _cannotExecute: CannotExecute)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.ToolHost

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                loop@ for (baseCommandLine in workflow.commandLines) {
                    when (baseCommandLine.executableFile.extension().lowercase()) {
                        "cmd", "bat" -> {
                            if (_virtualContext.targetOSType != OSType.WINDOWS) {
                                _cannotExecute.writeBuildProblemFor(baseCommandLine.executableFile)
                                break@loop
                            } else yield(
                                CommandLine(
                                    baseCommandLine,
                                    TargetType.ToolHost,
                                    Path("cmd.exe"),
                                    baseCommandLine.workingDirectory,
                                    getArguments(baseCommandLine).toList(),
                                    baseCommandLine.environmentVariables,
                                    baseCommandLine.title
                                )
                            )
                        }

                        else -> yield(baseCommandLine)
                    }
                }
            })

    private fun getArguments(commandLine: CommandLine) = sequence {
        yield(CommandLineArgument("/D"))
        yield(CommandLineArgument("/C"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}