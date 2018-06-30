package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

interface WorkflowComposer {
    val target: TargetType

    fun compose(context: WorkflowContext, workflow: Workflow = Workflow()): Workflow
}