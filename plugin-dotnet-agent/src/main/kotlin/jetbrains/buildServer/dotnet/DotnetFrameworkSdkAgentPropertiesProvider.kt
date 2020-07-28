package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.ToolInstanceProvider

class DotnetFrameworkSdkAgentPropertiesProvider(
        private val _sdkInstanceProviders: List<ToolInstanceProvider>)
    : AgentPropertiesProvider {
    override val desription = "Dotnet Framework SDK"

    override val properties: Sequence<AgentProperty> get() =
        _sdkInstanceProviders
                .asSequence()
                .flatMap { it.getInstances() }
                .filter { it.toolType == ToolInstanceType.DotNetFrameworkSDK }
                .flatMap {
                    sdk ->
                    sequence {
                        yield(AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK${sdk.baseVersion}_${sdk.platform.id}", sdk.detailedVersion.toString()))
                        yield(AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK${sdk.baseVersion}_${sdk.platform.id}_Path", sdk.installationPath.path))
                    }
                }
}