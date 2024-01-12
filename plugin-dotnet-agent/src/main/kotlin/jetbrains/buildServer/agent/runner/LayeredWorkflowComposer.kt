

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

class LayeredWorkflowComposer(
    private val _workflowComposers: List<SimpleWorkflowComposer>,
) : SimpleWorkflowComposer {
    override val target = TargetType.NotApplicable

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow {
        val toolWorkflows = _workflowComposers
            .filter { it.target == TargetType.Tool }
            .asSequence()
            .map { it.compose(context, state) }

        val otherWorkflowComposers = _workflowComposers
            .filter { it.target != TargetType.NotApplicable && it.target != TargetType.Tool }
            .sortedBy { it.target.priority }

        return toolWorkflows
            .map { tw -> otherWorkflowComposers.fold(tw) { acc, owc -> owc.compose(context, state, acc) } }
            .flatMap { it.commandLines }
            .let(::Workflow)
    }
}