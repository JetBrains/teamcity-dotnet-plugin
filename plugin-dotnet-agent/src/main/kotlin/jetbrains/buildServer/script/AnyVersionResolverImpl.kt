package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants.RUNNER_DESCRIPTION
import jetbrains.buildServer.dotnet.DotnetRuntimesProvider
import jetbrains.buildServer.excluding
import java.io.File
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class AnyVersionResolverImpl(
        private val _fileSystemService: FileSystemService,
        private val _runtimesProvider: DotnetRuntimesProvider,
        private val _virtualContext: VirtualContext)
    : AnyVersionResolver {
    override fun resolve(toolPath: File): CsiTool {
        var supportedRuntimes = getSupportedRuntimes(toolPath)

        if(!_virtualContext.isVirtual) {
            var runtimes = _runtimesProvider.getRuntimes().map { Version(it.version.major, it.version.minor) }.toList()
            supportedRuntimes =
                    supportedRuntimes
                    .filter {
                        val minVersion = it.runtimeVersion
                        val runtimeRange = minVersion.including() to Version(minVersion.major + 1).excluding()
                        val result = runtimes.any {runtimeRange.contains(it)}
                        LOG.debug("Min version: $minVersion, Runtime range: ${runtimeRange}, Result: $result")
                        result
                    }
        }

        return supportedRuntimes
                .maxByOrNull { it.runtimeVersion }
                ?: throw RunBuildException("Cannot find a supported version of $RUNNER_DESCRIPTION.")
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