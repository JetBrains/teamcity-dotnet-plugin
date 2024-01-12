

package jetbrains.buildServer.dotnet.commands.vstest

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.EnvironmentCleaner

class VSTestLoggerEnvironmentCleaner(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerService: LoggerService
) : EnvironmentCleaner {
    override fun clean() {
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        val loggersToClean = _fileSystemService.list(checkoutDirectory).filter { _fileSystemService.isDirectory(it) && it.name.startsWith(
            VSTestLoggerEnvironmentBuilder.directoryPrefix
        ) }.toList()
        for (loggerToClean in loggersToClean) {
            LOG.debug("Removing \"$loggerToClean\"")
            try {
                _fileSystemService.remove(loggerToClean)
            } catch (ex: Exception) {
                LOG.warn(ex)
                _loggerService.writeErrorOutput("Failed to remove logger directory \"$loggerToClean\"")
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(EnvironmentCleaner::class.java)
    }

}