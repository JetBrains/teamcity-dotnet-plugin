

package jetbrains.buildServer.agent

interface AgentPropertiesProvider {
    val description: String

    val properties: Sequence<AgentProperty>
}