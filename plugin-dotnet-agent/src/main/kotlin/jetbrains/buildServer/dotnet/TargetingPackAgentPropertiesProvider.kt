package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.runner.AgentPropertyType
import org.apache.log4j.Logger

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
                    LOG.info("Found .NET Framework targeting pack ${framework.version.toString()} at \"${framework.path.path}\".")
                    AgentProperty(AgentPropertyType.TargetingPack, "DotNetFrameworkTargetingPack${framework.version.major}${Version.Separator}${framework.version.minor}_Path", framework.path.path)
                }

    companion object {
        private val LOG = Logger.getLogger(TargetingPackAgentPropertiesProvider::class.java)
    }
}