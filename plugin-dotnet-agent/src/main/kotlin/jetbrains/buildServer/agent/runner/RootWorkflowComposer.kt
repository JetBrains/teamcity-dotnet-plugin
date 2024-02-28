package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.CommandLineLayer
import jetbrains.buildServer.agent.CommandLineLayer.Companion.within

class RootWorkflowComposer(
    private val _toolWorkflowComposers: List<BuildToolWorkflowComposer>,
    private val _postProcessingWorkflowComposers: List<PostProcessingWorkflowComposer>,
    private val _layeredWorkflowComposers: List<LayeredWorkflowComposer>,
) : WorkflowComposer<Unit> {
    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
        orderedWorkflowComposers
            .applyLayers(layers, context, state)
            .let(::Workflow)

    private val orderedWorkflowComposers get() = _toolWorkflowComposers + _postProcessingWorkflowComposers

    private val layers get() = _layeredWorkflowComposers.sortedBy { it.layer.priority }

    private fun List<WorkflowComposer<Unit>>.applyLayers(layers: List<LayeredWorkflowComposer>, context: WorkflowContext, state: Unit) = this
        .map { it.compose(context, state) }
        .map { tw -> layers.fold(tw) { acc, layer -> layer.compose(context, state, acc) } }
        .flatMap { it.commandLines }
        .asSequence()
}