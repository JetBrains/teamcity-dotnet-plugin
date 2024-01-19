

package jetbrains.buildServer.dotnet.commands.vstest

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestReportingParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.emptyDisposable
import java.io.File
import java.io.OutputStreamWriter

class VSTestLoggerEnvironmentBuilder(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _loggerResolver: LoggerResolver,
    private val _loggerService: LoggerService,
    private val _testReportingParameters: TestReportingParameters,
    private val _environmentCleaner: EnvironmentCleaner,
    private val _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer
) : EnvironmentBuilder {
    override fun build(context: DotnetCommandContext): EnvironmentBuildResult {
        val testReportingMode = _testReportingParameters.getMode(context)
        LOG.debug("Test reporting mode: $testReportingMode")

        if (testReportingMode.contains(TestReportingMode.Off)) {
            return EnvironmentBuildResult()
        }

        if (testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
            return EnvironmentBuildResult()
        }

        val targets = context.command.targetArguments.flatMap { it.arguments }.map { File(it.value) }.toList()
        LOG.debug("Targets: ${targets.joinToString(", ") { it.name }}")
        val checkoutDirectory = _pathsService.getPath(PathType.Checkout)
        LOG.debug("Checkout directory: $checkoutDirectory")
        val loggerDirectory = File(checkoutDirectory, "$directoryPrefix${_pathsService.uniqueName}")
        LOG.debug("Logger directory: $loggerDirectory")

        LOG.debug("Clean ...")
        _environmentCleaner.clean()
        LOG.debug("Analyze ...")
        _environmentAnalyzer.analyze(targets)

        val disposable = _loggerResolver.resolve(ToolType.VSTest).parentFile?.absoluteFile?.let {
            try {
                LOG.debug("Copy logger to \"$loggerDirectory\" from \"$it\"")
                _fileSystemService.copy(it, loggerDirectory)
                LOG.debug("Create \"$readmeFileName\" file in the directory \"$loggerDirectory\"")
                _fileSystemService.write(File(loggerDirectory, readmeFileName)) {
                    OutputStreamWriter(it).use {
                        it.write(readmeFileContent)
                    }
                }
            } catch (ex: Exception) {
                LOG.error(ex)
                _loggerService.writeErrorOutput("Failed to create logger directory \"$loggerDirectory\"")
            }

            disposableOf { _fileSystemService.remove(loggerDirectory) }
        } ?: emptyDisposable()
        return EnvironmentBuildResult(disposable = disposable)
    }

    companion object {
        private val LOG = Logger.getLogger(VSTestLoggerEnvironmentBuilder::class.java)
        internal const val directoryPrefix = "teamcity.logger."
        internal const val readmeFileName = "readme.txt"
        internal const val readmeFileContent = "This directory is created by TeamCity agent.\n" +
                "It contains files necessary for real-time tests reporting.\n" +
                "The directory will be removed automatically."
    }
}