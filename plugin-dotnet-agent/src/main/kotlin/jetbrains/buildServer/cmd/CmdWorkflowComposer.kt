package jetbrains.buildServer.cmd

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.util.OSType

class CmdWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, workflow: Workflow) =
            when (_virtualContext.targetOSType) {
                OSType.WINDOWS -> {
                    Workflow(sequence {
                        var cmdExecutable: Path? = null
                        for (baseCommandLine in workflow.commandLines) {
                            when (baseCommandLine.executableFile.extension().toLowerCase()) {
                                "cmd", "bat" -> {
                                    yield(CommandLine(
                                            baseCommandLine,
                                            TargetType.Host,
                                            cmdExecutable ?: Path( "cmd.exe"),
                                            baseCommandLine.workingDirectory,
                                            getArguments(baseCommandLine).toList(),
                                            baseCommandLine.environmentVariables,
                                            baseCommandLine.title,
                                            baseCommandLine.description))
                                }
                                else -> yield(baseCommandLine)
                            }
                        }
                    })
                }
                else -> workflow
            }

    private fun getArguments(commandLine: CommandLine) = sequence {
        yield(CommandLineArgument("/D"))
        yield(CommandLineArgument("/C"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}