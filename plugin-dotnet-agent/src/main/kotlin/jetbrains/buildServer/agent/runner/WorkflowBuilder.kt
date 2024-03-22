package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*

internal fun workflow(init: WorkflowBuilder.() -> WorkflowBuilder)= init(WorkflowBuilder()).build()

internal class WorkflowBuilder {
    private var commandLines: Sequence<CommandLine> = emptySequence()
    private var finished = false;

    fun build(): Workflow = Workflow(commandLines)

    fun guard(finalWorkflow: Workflow = Workflow.empty, predicate: () -> Boolean): WorkflowBuilder {
        if (!predicate()) {
            commandLines = finalWorkflow.commandLines
            finished = true;
        }
        return this
    }

    fun commandLine(init: CommandLineBuilder.() -> CommandLineBuilder): WorkflowBuilder {
        val commandLineBuilder = CommandLineBuilder()
        commandLines.plus(sequence { init(commandLineBuilder).build() })
        return this
    }
}

internal class CommandLineBuilder {
    var baseCommandLine: CommandLine? = null
    lateinit var title: String
    lateinit var description: ArrayList<StdOutText>
    lateinit var executableFile: Path
    lateinit var workingDirectory: Path
    lateinit var arguments: List<CommandLineArgument>
    lateinit var environmentVariables: List<CommandLineEnvironmentVariable>

    fun build() = CommandLine(  // todo
        baseCommandLine = baseCommandLine,
        target = TargetType.Tool,
        executableFile = executableFile,
        workingDirectory = workingDirectory,
        arguments = arguments,
        environmentVariables = environmentVariables,
        title = title,
        description = description,
    )

    fun before(init: () -> Unit): CommandLineBuilder {

    }

    fun onExitCode(init: (Int) -> Unit): CommandLineBuilder {

    }

    fun after(init: () -> Unit): CommandLineBuilder {

    }
}

class ExampleWorkflowComposer : WorkflowComposer<Unit> {
    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) = workflow {
        guard {
            true
        }

        commandLine {
            title = "df"
            executableFile
            arguments = emptyList()
            environmentVariables = emptyList()

            before {

            }

            after {

            }
        }

        commandLine {
            title = ""

            this
        }
    }
}