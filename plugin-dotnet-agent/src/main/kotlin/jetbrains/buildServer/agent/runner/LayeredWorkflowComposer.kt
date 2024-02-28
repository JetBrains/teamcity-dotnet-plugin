package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.CommandLineLayer

interface LayeredWorkflowComposer: WorkflowComposer<Unit> {
    val layer: CommandLineLayer
}