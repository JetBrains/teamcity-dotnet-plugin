package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.AgentPropertyType

data class AgentProperty(val type: AgentPropertyType, val name: String, val value: String)