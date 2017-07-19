package jetbrains.buildServer.runners

import java.util.stream.Stream

interface WorkflowComposer {
    val target: TargetType

    fun compose(context: WorkflowContext, workflow: Workflow = Workflow()): Workflow;
}