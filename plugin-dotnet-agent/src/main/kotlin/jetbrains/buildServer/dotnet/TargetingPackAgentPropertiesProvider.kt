package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK
import jetbrains.buildServer.agent.Logger

class TargetingPackAgentPropertiesProvider(
        private val _frameworksProvider: DotnetFrameworksProvider)
    : AgentPropertiesProvider {

    override val desription = "Dotnet Framework targeting pack"

    override val properties: Sequence<AgentProperty> get() =
        _frameworksProvider
                .getFrameworks()
                .filter { it.version.major == 2 && it.version.minor == 0 }
                .distinctBy { Version(it.version.major, it.version.minor) }
                .map {
                    framework ->
                    LOG.debug("Found .NET Framework targeting pack ${framework.version.toString()} at \"${framework.path.path}\".")
                    AgentProperty(ToolInstanceType.TargetingPack, "$CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK${framework.version.major}${Version.Separator}${framework.version.minor}_Path", framework.path.path)
                }

    companion object {
        private val LOG = Logger.getLogger(TargetingPackAgentPropertiesProvider::class.java)
    }
}