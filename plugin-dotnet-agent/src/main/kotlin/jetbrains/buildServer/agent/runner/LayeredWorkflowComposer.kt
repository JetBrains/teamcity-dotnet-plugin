package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

class LayeredWorkflowComposer(
        private val _workflowComposers: List<WorkflowComposer>)
    : WorkflowComposer {
    override val target: TargetType = TargetType.NotApplicable

    override fun compose(
            context: WorkflowContext,
            workflow: Workflow): Workflow {
        val toolWorkflows = _workflowComposers
                .filter { it.target == TargetType.Tool }
                .asSequence()
                .map { it.compose(context) }

        val otherWorkflowComposers = _workflowComposers
                .filter { it.target != TargetType.NotApplicable && it.target != TargetType.Tool }
                .sortedBy { it.target.priority }

        val workflows = toolWorkflows.map { compose(context, it, otherWorkflowComposers) }
        val commandLines = workflows.flatMap { it.commandLines }
        return Workflow(commandLines)
    }

    private fun compose(context: WorkflowContext, toolWorkflow: Workflow, otherWorkflowComposers: List<WorkflowComposer>): Workflow =
            otherWorkflowComposers.fold(toolWorkflow) { acc, it -> it.compose(context, acc) }
}