package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.CommandLineLayer

interface WorkflowComposer<TState> {
    fun compose(context: WorkflowContext, state: TState, workflow: Workflow = Workflow()): Workflow
}