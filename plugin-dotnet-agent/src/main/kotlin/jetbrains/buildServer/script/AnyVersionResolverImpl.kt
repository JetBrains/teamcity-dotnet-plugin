package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetRuntimesProvider
import jetbrains.buildServer.excluding
import java.io.File
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class AnyVersionResolverImpl(
        private val _fileSystemService: FileSystemService,
        private val _runtimesProvider: DotnetRuntimesProvider)
    : AnyVersionResolver {
    override fun resolve(toolsPath: File): File {
        var runtimes = _runtimesProvider.getRuntimes().map { Version(it.version.major, it.version.minor) }.toList()
        return _fileSystemService.list(toolsPath)
                .filter { _fileSystemService.isDirectory(it) }
                .map {
                    LOG.debug("Goes through $it")
                    val version = VersionRegex.matchEntire(it.name)?.let {
                        Version.parse(it.groupValues[1])
                    } ?: Version.Empty

                    LOG.debug("Version: $version")
                    it to version
                }
                .filter { it.second != Version.Empty }
                .filter {
                    val minVersion = it.second
                    val runtimeRange = minVersion.including() to Version(minVersion.major + 1).excluding()
                    val result = runtimes.any {runtimeRange.contains(it)}
                    LOG.debug("Min version: $minVersion, Runtime range: ${runtimeRange}, Result: $result")
                    result
                }
                .maxByOrNull { it.second }
                ?.first
                ?: throw RunBuildException("Cannot find a supported versin of C# script runner.")
    }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        private val VersionRegex = Regex("^\\w+?([\\d\\.]+)$", RegexOption.IGNORE_CASE)
    }
}