package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType.DotNetCLI
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_WORKLOADS
import jetbrains.buildServer.dotnet.DotnetConstants.EXECUTABLE
import jetbrains.buildServer.dotnet.DotnetWorkloadProvider
import java.io.File

class DotnetWorkloadAgentPropertiesProvider(
    private val _toolProvider: ToolProvider,
    private val _dotnetWorkloadProvider: DotnetWorkloadProvider,
) : AgentPropertiesProvider {

    override val desription = ".NET Workload"

    override val properties: Sequence<AgentProperty>
        get() {
            return _dotnetWorkloadProvider.getInstalledWorkloads(dotnetExecutableFile())
                .groupBy { it.sdkVersion }
                .map { groupedWorkloads ->
                    AgentProperty(
                        DotNetCLI,
                        CONFIG_PREFIX_DOTNET_WORKLOADS + "_" + groupedWorkloads.key,
                        groupedWorkloads.value.joinToString(separator = ",") { it.name })
                }.asSequence()
        }

    private fun dotnetExecutableFile() = File(_toolProvider.getPath(EXECUTABLE))
}