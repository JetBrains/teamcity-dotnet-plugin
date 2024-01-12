

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

interface WorkflowComposer<TState> {
    val target: TargetType

    fun compose(context: WorkflowContext, state: TState, workflow: Workflow = Workflow()): Workflow
}