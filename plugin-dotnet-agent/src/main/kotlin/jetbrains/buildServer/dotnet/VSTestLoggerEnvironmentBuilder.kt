package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.Closeable
import java.io.File
import java.io.OutputStreamWriter

class VSTestLoggerEnvironmentBuilder(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _loggerResolver: LoggerResolver,
        private val _loggerService: LoggerService,
        private val _testReportingParameters: TestReportingParameters,
        private val _environmentCleaner: EnvironmentCleaner,
        private val _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer)
    : EnvironmentBuilder {
    override fun build(command: DotnetCommand): Closeable {
        if (_testReportingParameters.mode == TestReportingMode.Off) {
           return EmptyClosable
        }

        val targets = command.targetArguments.flatMap { it.arguments }.map { File(it.value) }.toList()
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        val loggerDirectory = File(checkoutDirectory, "$DirectoryPrefix${_pathsService.uniqueName}")

        _environmentCleaner.clean()
        _environmentAnalyzer.analyze(targets)

        return _loggerResolver.resolve(ToolType.VSTest).parentFile?.absoluteFile?.let {
            try {
                LOG.debug("Copy logger to \"$loggerDirectory\" from \"$it\"")
                _fileSystemService.copy(it, loggerDirectory)
                LOG.debug("Create \"$ReadmeFileName\" file in the directory \"$loggerDirectory\"")
                _fileSystemService.write(File(loggerDirectory, ReadmeFileName)) {
                    OutputStreamWriter(it).use {
                        it.write(ReadmeFileContent)
                    }
                }
            } catch (ex: Exception) {
                LOG.error(ex)
                _loggerService.onErrorOutput("Failed to create logger directory \"$loggerDirectory\"")
            }

            return Closeable { _fileSystemService.remove(loggerDirectory) }
        } ?: EmptyClosable
    }

    companion object {
        private val LOG = Logger.getInstance(VSTestLoggerEnvironmentBuilder::class.java.name)
        val DirectoryPrefix = "teamcity.logger."
        val ReadmeFileName = "readme.txt"
        val ReadmeFileContent = "This directory is created by TeamCity agent.\nIt contains files necessary for real-time tests reporting.\nThe directory will be removed automatically."
        private val EmptyClosable = Closeable { }
    }
}