package jetbrains.buildServer

import jetbrains.buildServer.agent.CommandLineLayer
import jetbrains.buildServer.agent.runner.LayeredWorkflowComposer

class ProfilerHostWorkflowComposer(
    private val _executableWorkflowComposer: LayeredWorkflowComposer
) : LayeredWorkflowComposer by _executableWorkflowComposer {
    override val layer = CommandLineLayer.ProfilerHost
}