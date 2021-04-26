package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import java.io.File

class DotnetRuntimeAgentPropertiesProvider(
        private val _toolProvider: ToolProvider,
        private val _runtimesProvider: DotnetRuntimesProvider,
        private val _versionEnumerator: VersionEnumerator)
    : AgentPropertiesProvider {

    override val desription = ".NET Runtime"

    override val properties: Sequence<AgentProperty>
        get() = sequence {
            // Detect .NET CLI path
            val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))

            // Detect .NET Runtimes
            for ((version, runtime) in _versionEnumerator.enumerate(_runtimesProvider.getRuntimes(dotnetPath))) {
                val paramName = "$CONFIG_PREFIX_CORE_RUNTIME$version${DotnetConstants.CONFIG_SUFFIX_PATH}"
                yield(AgentProperty(ToolInstanceType.DotNetRuntime, paramName, runtime.path.absolutePath))
            }
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdkAgentPropertiesProvider::class.java)
    }
}