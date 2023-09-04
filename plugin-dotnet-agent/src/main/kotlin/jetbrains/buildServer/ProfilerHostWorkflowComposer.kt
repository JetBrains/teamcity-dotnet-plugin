package jetbrains.buildServer

import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.SimpleWorkflowComposer

class ProfilerHostWorkflowComposer(
    private val _executableWorkflowComposer: SimpleWorkflowComposer
) : SimpleWorkflowComposer by _executableWorkflowComposer {
    override val target = TargetType.ProfilerHost
}