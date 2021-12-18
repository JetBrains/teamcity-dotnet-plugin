package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.agent.Logger

class DotnetFrameworkRegistryAgentPropertiesProvider(
        private val _dotnetFrameworksProvider: DotnetFrameworksProvider)
    : AgentPropertiesProvider {
    override val desription = "Dotnet Framework in registry"

    override val properties: Sequence<AgentProperty> get() =
        _dotnetFrameworksProvider
                .getFrameworks()
                .groupBy { it.platform }
                .map { curFrameworks ->
                    val latestDotnet4Version = curFrameworks.value.map { it.version }.filter { it.major == 4 }.maxByOrNull { it }

                    // Report only the latest version of .NET Framework 4.x
                    return@map curFrameworks
                            .value
                            .asSequence()
                            .filter { it.version.major != 4 || it.version == latestDotnet4Version }
                            .map {
                                LOG.debug("Found .NET Framework ${it.version} ${it.platform.id} at \"${it.path}\".")
                                sequence {
                                    val majorVersion = "${it.version.major}${Version.Separator}${it.version.minor}"
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${majorVersion}_${it.platform.id}", it.version.toString()))
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${majorVersion}_${it.platform.id}_Path", it.path.path))
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${it.version}_${it.platform.id}_Path", it.path.path))
                                }
                            }
                            .flatMap { it }
                }
                .asSequence()
                .flatMap { it }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworkRegistryAgentPropertiesProvider::class.java)
    }
}
