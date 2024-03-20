package jetbrains.buildServer.agent.runner

interface WorkflowComposer<TState> {
    fun compose(context: WorkflowContext, state: TState, workflow: Workflow = Workflow()): Workflow
}
