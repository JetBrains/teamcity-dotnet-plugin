package jetbrains.buildServer.agent.runner

interface ToolInstanceProvider {
    fun getInstances(): Collection<ToolInstance>
}