package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstanceProvider

class WindowsSdkAgentPropertiesProvider(
        private val _sdkInstanceProvider: ToolInstanceProvider)
    : AgentPropertiesProvider {
    override val desription = "Windows SDK"

    override val properties: Sequence<AgentProperty> get() =
        _sdkInstanceProvider
                .getInstances()
                .asSequence()
                .filter { it.toolType == ToolInstanceType.WindowsSDK }
                .flatMap {
                    sdk ->
                    sequence {
                        val version = "WindowsSDKv${sdk.baseVersion.major}${Version.Separator}${sdk.baseVersion.minor}${sdk.baseVersion.release ?: ""}"
                        yield(AgentProperty(ToolInstanceType.WindowsSDK, version, sdk.detailedVersion.toString()))
                        yield(AgentProperty(ToolInstanceType.WindowsSDK, "${version}_Path", sdk.installationPath.path))
                    }
                }
}