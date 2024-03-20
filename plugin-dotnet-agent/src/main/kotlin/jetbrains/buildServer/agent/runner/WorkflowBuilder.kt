package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*

internal fun workflow(init: WorkflowBuilder.() -> WorkflowBuilder): Workflow {
    val builder = WorkflowBuilder.new.init()
    return builder.build()
}

internal class WorkflowBuilder private constructor(private val buildAction: () -> Workflow) {
    private val workflow by lazy { buildAction() }
    private val sequence: Sequence<CommandLine> = sequenceOf() // todo
    private var finished = false;

    fun build(): Workflow = workflow

    fun guard(workflowToReturn: Workflow = Workflow.empty, predicate: () -> Boolean) {
        if (!predicate()) {
            finished = true;
        }
    }

    fun environment(init: () -> Unit) {

    }

    fun commandLine(init: CommandLineBuilder.() -> CommandLineBuilder) = CommandLineBuilder {
//        sequence.plus(sequence { init().build() })
    }

    companion object {
        val new get() = WorkflowBuilder { Workflow() }
    }
}

internal class CommandLineBuilder {
    var title = ""
    var description = ArrayList<StdOutText>()
    lateinit var arguments: List<CommandLineArgument>
    var environmentVariables = ArrayList<CommandLineEnvironmentVariable>()

    fun build() = CommandLine(  // todo
        baseCommandLine = null,
        target = TargetType.Tool,
        executableFile = Path(""),
        workingDirectory = Path(""),
        arguments = arguments,
        environmentVariables = environmentVariables,
        title = title,
        description = emptyList<StdOutText>(),
    )

    fun prepare(init: () -> Unit): CommandLineBuilder {

    }

    fun onExitCode(init: (Int) -> Unit): CommandLineBuilder {

    }

    fun after(init: () -> Unit): CommandLineBuilder {

    }

    companion object {
        val new get() = CommandLineBuilder { CommandLine() } // TODO
    }
}

class ExampleWorkflowComposer : WorkflowComposer<Unit> {
    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) = workflow {
        guard {
            true
        }

        commandLine {
            title = "df"
            arguments = emptyList()
        }.prepare {

        }.onExitCode {

        }.after {

        }
    }
}