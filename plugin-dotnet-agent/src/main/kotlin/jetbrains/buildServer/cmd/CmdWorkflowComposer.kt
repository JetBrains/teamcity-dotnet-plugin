package jetbrains.buildServer.cmd

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType
import java.io.File
import java.io.OutputStreamWriter
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class CmdWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _environment: Environment)
    : WorkflowComposer {
    override val target: TargetType
        get() = TargetType.Host

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
        when (_environment.OS) {
            OSType.WINDOWS -> {
                Workflow (
                    buildSequence {
                        val cmdExecutable = _environment.tryGetVariable(ComSpecEnvVarName) ?: throw RunBuildException("Environment variable \"$ComSpecEnvVarName\" was not found");
                        for (commandLine in workflow.commandLines) {
                            when(commandLine.executableFile.extension.toLowerCase() ?: "") {
                                "cmd", "bat" -> {
                                    yield(CommandLine(
                                            TargetType.Host,
                                            File(cmdExecutable),
                                            commandLine.workingDirectory,
                                            getArguments(commandLine).toList(),
                                            commandLine.environmentVariables))
                                }
                                else -> yield(commandLine)
                            }
                        }
                    }
                )
            }
            else -> workflow
        }

    private fun getArguments(commandLine: CommandLine): Sequence<CommandLineArgument> =
        buildSequence {
            yield(CommandLineArgument("/D"))
            yield(CommandLineArgument("/C"))
            val args = sequenceOf(commandLine.executableFile.absolutePath).plus(commandLine.arguments.map { it.value })
            yield(CommandLineArgument("\"${_argumentsService.combine(args)}\""))
        }

    companion object {
        internal const val ComSpecEnvVarName = "ComSpec"
    }
}