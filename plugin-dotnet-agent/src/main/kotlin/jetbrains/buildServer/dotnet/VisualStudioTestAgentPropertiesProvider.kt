package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_VSTEST

class VisualStudioTestAgentPropertiesProvider(
        private val _visualStudioTestInstanceProvider: ToolInstanceProvider)
    : AgentPropertiesProvider {
    override val desription = "Visual Studio Test Console"

    override val properties: Sequence<AgentProperty> get() =
        _visualStudioTestInstanceProvider
                .getInstances()
                .asSequence()
                .filter { it.toolType == ToolInstanceType.VisualStudioTest }
                .map {
                    console ->
                    AgentProperty(ToolInstanceType.VisualStudioTest, "$CONFIG_PREFIX_DOTNET_VSTEST.${console.baseVersion.major}${Version.Separator}${console.baseVersion.minor}", console.installationPath.path)
                }
}