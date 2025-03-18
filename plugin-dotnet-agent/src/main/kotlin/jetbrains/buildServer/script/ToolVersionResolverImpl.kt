package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants.RUNNER_DESCRIPTION
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider
import jetbrains.buildServer.excluding
import java.io.File
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class ToolVersionResolverImpl(
    private val _fileSystemService: FileSystemService,
    private val _runtimesProvider: DotnetRuntimesProvider,
    private val _virtualContext: VirtualContext
) : ToolVersionResolver {
    override fun resolve(toolPath: File): CsiTool {
        var supportedRuntimes = getSupportedRuntimes(toolPath).toList()

        var tool: CsiTool?
        if (!_virtualContext.isVirtual) {
            var agentRuntimes = _runtimesProvider.getRuntimes().map { Version(it.version.major, it.version.minor) }.distinct().toList()

            // try to find major match first
            tool = supportedRuntimes.filter {
                val runtimeVersion = it.runtimeVersion
                val runtimeRange = runtimeVersion.including() to Version(runtimeVersion.major + 1).excluding()
                val anyAgentRuntimeMatches = agentRuntimes.any { runtimeRange.contains(it) }
                LOG.debug("[Major matching] version: $runtimeVersion, range: ${runtimeRange}, any agent runtime matches: $anyAgentRuntimeMatches")
                anyAgentRuntimeMatches
            }.maxByOrNull { it.runtimeVersion }

            // fallback to major roll-forward if no matches found
            if (tool == null) {
                tool = supportedRuntimes.filter {
                    val runtimeVersion = it.runtimeVersion
                    val runtimeRange = runtimeVersion.including() to Version(Int.MAX_VALUE).including()
                    val anyAgentRuntimeMatches = agentRuntimes.any { runtimeRange.contains(it) }
                    LOG.debug("[Major roll-forward matching] version: $runtimeVersion, range: ${runtimeRange}, any agent runtime matches: $anyAgentRuntimeMatches")
                    anyAgentRuntimeMatches
                }.maxByOrNull { it.runtimeVersion }
            }
        } else {
            // Should return .NET Runtime LTS version for docker 6.0.0, 8.0.0 etc. according to https://dotnet.microsoft.com/en-us/platform/support/policy
            // or any newest available version
            tool = supportedRuntimes.filter { it.runtimeVersion.major % 2 == 0 }.maxByOrNull { it.runtimeVersion } ?: supportedRuntimes.maxByOrNull { it.runtimeVersion }
        }

        return tool ?: throw RunBuildException("Cannot find a supported version of $RUNNER_DESCRIPTION.")
    }

    private fun getSupportedRuntimes(toolsPath: File) =
        _fileSystemService.list(toolsPath)
            .filter { _fileSystemService.isDirectory(it) }
            .map {
                LOG.debug("Goes through $it")
                val version = VersionRegex.matchEntire(it.name)?.let {
                    Version.parse(it.groupValues[1])
                } ?: Version.Empty

                LOG.debug("Version: $version")
                CsiTool(it, version)
            }
            .filter { it.runtimeVersion != Version.Empty }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        private val VersionRegex = Regex("^\\w+?([\\d\\.]+)$", RegexOption.IGNORE_CASE)
    }
}