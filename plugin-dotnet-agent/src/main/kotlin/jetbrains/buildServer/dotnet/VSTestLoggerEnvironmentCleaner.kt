package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService

class VSTestLoggerEnvironmentCleaner(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService
) : EnvironmentCleaner {
    override fun clean() {
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        val loggersToClean = _fileSystemService.list(checkoutDirectory).filter { _fileSystemService.isDirectory(it) && it.name.startsWith(VSTestLoggerEnvironmentBuilder.DirectoryPrefix) }.toList()
        for (loggerToClean in loggersToClean) {
            LOG.debug("Removing \"$loggerToClean\"")
            try {
                _fileSystemService.remove(loggerToClean)
            }
            catch (ex: Exception) {
                LOG.error(ex)
                _loggerService.onErrorOutput("Failed to remove logger directory \"$loggerToClean\"")
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(EnvironmentCleaner::class.java.name)
    }

}