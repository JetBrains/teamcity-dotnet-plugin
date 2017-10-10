package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.Closeable
import java.io.File

class VSTestLoggerEnvironmentImpl(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerResolver: LoggerResolver)
    : VSTestLoggerEnvironment {
    override fun configure(targets: List<File>): Closeable =
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            val loggerLocalPaths = mutableListOf<File>()
            val paths = targets.map { it.absoluteFile.parentFile }.toMutableSet()
            paths.add(_pathsService.getPath(PathType.WorkingDirectory).absoluteFile)
            for (path in paths) {
                val localLoggerDirectory = File(path, _pathsService.uniqueName)
                _fileSystemService.copy(it, localLoggerDirectory)
                loggerLocalPaths.add(localLoggerDirectory)
            }

            return Closeable { loggerLocalPaths.forEach { _fileSystemService.remove(it) } }
        } ?: EmptyClosable

    companion object {
        private val EmptyClosable = Closeable { }
    }
}