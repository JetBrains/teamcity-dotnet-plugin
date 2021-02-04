package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.runner.ToolInstance

interface ToolInstanceProvider {
    fun getInstances(): List<ToolInstance>
}