package jetbrains.buildServer.agent.runner

interface WorkflowFactory<TState> {
    fun create(context: WorkflowContext, state: TState): Workflow
}