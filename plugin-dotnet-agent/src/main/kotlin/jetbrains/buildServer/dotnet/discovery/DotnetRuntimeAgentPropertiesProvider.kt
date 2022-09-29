package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider
import jetbrains.buildServer.dotnet.VersionEnumerator

class DotnetRuntimeAgentPropertiesProvider(
    private val _runtimesProvider: DotnetRuntimesProvider,
    private val _versionEnumerator: VersionEnumerator
)
    : AgentPropertiesProvider {

    override val desription = ".NET Runtime"

    override val properties: Sequence<AgentProperty>
        get() = sequence {
            for ((version, runtime) in _versionEnumerator.enumerate(_runtimesProvider.getRuntimes())) {
                val paramName = "$CONFIG_PREFIX_CORE_RUNTIME$version${DotnetConstants.CONFIG_SUFFIX_PATH}"
                yield(AgentProperty(ToolInstanceType.DotNetRuntime, paramName, runtime.path.absolutePath))
            }
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdkAgentPropertiesProvider::class.java)
    }
}