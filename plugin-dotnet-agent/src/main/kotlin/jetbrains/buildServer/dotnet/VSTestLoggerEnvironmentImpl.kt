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
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.absoluteFile?.let {
            val loggerLocalPaths = mutableListOf<File>()
            val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
            val paths = targets.map { getFullTargetPath(workingDirectory, it) }.toMutableSet()
            paths.add(workingDirectory.absoluteFile)
            for (path in paths) {
                val localLoggerDirectory = File(path, _pathsService.uniqueName)
                _fileSystemService.copy(it, localLoggerDirectory)
                loggerLocalPaths.add(localLoggerDirectory)
            }

            return Closeable { loggerLocalPaths.forEach { _fileSystemService.remove(it) } }
        } ?: EmptyClosable

    private fun getDirectory(targetPath: File): File =
            if (_fileSystemService.isDirectory(targetPath)) targetPath else targetPath.parentFile

    private fun getFullTargetPath(workingDirectory: File, targetPath: File): File =
            if (_fileSystemService.isAbsolute(targetPath)) getDirectory(targetPath) else getDirectory(File(workingDirectory, targetPath.path)).absoluteFile

    companion object {
        private val EmptyClosable = Closeable { }
    }
}