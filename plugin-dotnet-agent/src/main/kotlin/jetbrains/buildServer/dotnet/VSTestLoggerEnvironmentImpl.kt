package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.Closeable
import java.io.File
import java.util.TreeSet

class VSTestLoggerEnvironmentImpl(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerResolver: LoggerResolver)
    : VSTestLoggerEnvironment {
    override fun configure(paths: List<File>): Closeable =
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            var loggerLocalPaths = mutableListOf<File>()
            val targets = paths.map { it.absoluteFile.parentFile }.toMutableSet()
            if (targets.size == 0) {
                targets.add(_pathsService.getPath(PathType.WorkingDirectory).absoluteFile)
            }

            for (path in targets) {
                val localLoggerDirectory = File(path, _pathsService.uniqueName)
                _fileSystemService.copy(it, localLoggerDirectory)
                loggerLocalPaths.add(localLoggerDirectory)
            }

            return object: Closeable {
                override fun close() {
                    loggerLocalPaths.forEach() { _fileSystemService.remove(it) }
                }
            }
        } ?: EmptyClosable

    companion object {
        private val EmptyClosable = object : Closeable {
            override fun close() {
            }
        }
    }
}