package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

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