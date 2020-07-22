package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.runner.AgentPropertyType
import org.apache.log4j.Logger

class DotnetFrameworkRegistryAgentPropertiesProvider(
        private val _dotnetFrameworksProvider: DotnetFrameworksProvider)
    : AgentPropertiesProvider {
    override val desription = "Dotnet Framework in registry"

    override val properties: Sequence<AgentProperty> get() =
        _dotnetFrameworksProvider
                .frameworks
                .groupBy { it.platform }
                .map { curFrameworks ->
                    val latestDotnet4Version = curFrameworks.value.map { it.version }.filter { it.major == 4 }.maxBy { it }

                    // Report only the latest version of .NET Framework 4.x
                    return@map curFrameworks
                            .value
                            .asSequence()
                            .filter { it.version.major != 4 || it.version == latestDotnet4Version }
                            .map {
                                LOG.info("Found .NET Framework ${it.version} ${it.platform.id} at \"${it.path}\".")
                                sequence {
                                    val majorVersion = "${it.version.major}${Version.Separator}${it.version.minor}"
                                    yield(AgentProperty(AgentPropertyType.DotNetFramework, "DotNetFramework${majorVersion}_${it.platform.id}", it.version.toString()))
                                    yield(AgentProperty(AgentPropertyType.DotNetFramework, "DotNetFramework${majorVersion}_${it.platform.id}_Path", it.path.path))
                                    yield(AgentProperty(AgentPropertyType.DotNetFramework, "DotNetFramework${it.version}_${it.platform.id}_Path", it.path.path))
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
