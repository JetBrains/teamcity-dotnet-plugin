package jetbrains.buildServer.runners

class LayeredWorkflowComposer(
        private val _workflowComposers: List<WorkflowComposer>
) : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow =
            _workflowComposers
            .filter { it.target != TargetType.NotApplicable }
            .sortedBy { it.target }
            .fold(Workflow(), { acc, it -> it.compose(context, acc)})
}