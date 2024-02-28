package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType

class LayeredWorkflowComposer(
    private val _workflowComposers: List<SimpleWorkflowComposer>,
    private val _stepPostProcessingWorkflowComposers: List<BuildStepPostProcessingWorkflowComposer>
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
            .plus(postProcessingWorkflows(context, state).flatMap { it.commandLines })
            .let(::Workflow)
    }

    private fun postProcessingWorkflows(context: WorkflowContext, state: Unit): Sequence<Workflow> {
        val postProcessingWorkflows = _stepPostProcessingWorkflowComposers
            .asSequence()
            .map { it.compose(context, state) }

        val profilerHosts = _workflowComposers
            .filter { it.target == TargetType.ProfilerHost }
            .asSequence()

        return postProcessingWorkflows
            .map { ppWorkflows -> profilerHosts.fold(ppWorkflows) { acc, profilerHost -> profilerHost.compose(context, state, acc) } }
    }
}