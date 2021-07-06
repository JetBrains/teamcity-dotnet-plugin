package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.Version
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetRuntimesProviderImpl(
        private val _fileSystemService: FileSystemService,
        private val _toolProvider: ToolProvider)
    : DotnetRuntimesProvider {
    @Cacheable("ListOfDotnetRuntimes", sync = true)
    override fun getRuntimes(): Sequence<DotnetRuntime> {
        val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
        val runtimesPath = File(dotnetPath.parent, "shared")
        if (!_fileSystemService.isExists(runtimesPath) || !_fileSystemService.isDirectory(runtimesPath)) {
            LOG.warn("The directory <$runtimesPath> does not exists.")
        }

        LOG.debug("Try getting the list of .NET Runtimes from directory <$runtimesPath>.")
        return _fileSystemService.list(runtimesPath)
                .filter { _fileSystemService.isDirectory(it) }
                .mapNotNull { path ->
                    RuntimeRegex.matchEntire(path.name)?.let {
                        Pair(path, it)
                    }
                }
                .mapNotNull { item ->
                    _fileSystemService.list(item.first)
                            .filter { _fileSystemService.isDirectory(it) }
                            .map { DotnetRuntime(it, Version.parse(it.name), item.second.groupValues[1]) }
                            .filter { it.version != Version.Empty }
                }.flatMap { it }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetRuntimesProviderImpl::class.java)
        private val RuntimeRegex = Regex("^Microsoft\\.(NETCore)\\.App$", RegexOption.IGNORE_CASE)
    }
}