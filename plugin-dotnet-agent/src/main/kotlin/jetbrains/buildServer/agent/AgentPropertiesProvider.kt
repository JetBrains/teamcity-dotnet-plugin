package jetbrains.buildServer.agent

interface AgentPropertiesProvider {
    val desription: String

    val properties: Sequence<AgentProperty>
}