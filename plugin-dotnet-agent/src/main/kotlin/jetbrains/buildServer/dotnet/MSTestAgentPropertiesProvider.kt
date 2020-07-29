package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstanceProvider

class MSTestAgentPropertiesProvider(
        private val _visualStudioTestInstanceProvider: ToolInstanceProvider)
    : AgentPropertiesProvider {
    override val desription = "MSTest Console"

    override val properties: Sequence<AgentProperty> get() =
        _visualStudioTestInstanceProvider
                .getInstances()
                .filter { it.toolType == ToolInstanceType.MSTest }
                .map {
                    console ->
                    AgentProperty(ToolInstanceType.MSTest, "teamcity.dotnet.mstest.${console.baseVersion.major}${Version.Separator}${console.baseVersion.minor}", console.installationPath.path)
                }
}