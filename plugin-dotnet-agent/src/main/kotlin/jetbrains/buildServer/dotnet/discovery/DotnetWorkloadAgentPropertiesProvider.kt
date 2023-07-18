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
    private val _fileBasedDotnetWorkloadProvider: DotnetWorkloadProvider,
    private val _registryBasedDotnetWorkloadProvider: DotnetWorkloadProvider,
) : AgentPropertiesProvider {

    override val desription = ".NET Workload"

    override val properties: Sequence<AgentProperty>
        get() {
            val dotnetExecutable = dotnetExecutableFile()
            var workloads = _fileBasedDotnetWorkloadProvider.getInstalledWorkloads(dotnetExecutable)
            if (workloads.isEmpty())
                workloads = _registryBasedDotnetWorkloadProvider.getInstalledWorkloads(dotnetExecutable)
            return workloads
                .distinct()
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