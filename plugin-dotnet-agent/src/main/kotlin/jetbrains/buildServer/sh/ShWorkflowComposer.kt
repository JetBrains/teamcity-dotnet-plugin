package jetbrains.buildServer.sh

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.util.OSType

class ShWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, workflow: Workflow) =
            when (_virtualContext.targetOSType) {
                OSType.UNIX, OSType.MAC -> {
                    Workflow(sequence {
                        var shExecutable: Path? = null
                            for (baseCommandLine in workflow.commandLines) {
                                when (baseCommandLine.executableFile.extension().toLowerCase()) {
                                    "sh" -> {
                                        yield(CommandLine(
                                                baseCommandLine,
                                                TargetType.Host,
                                                shExecutable ?: Path( "sh"),
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
        yield(CommandLineArgument("-c"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}