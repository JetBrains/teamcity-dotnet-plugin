package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.serviceMessages.FileStreamingServiceMessage
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.EnvironmentBuilder
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.emptyDisposable
import java.io.File
import java.nio.file.Paths

class TestCommandEnvironmentBuilder(
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _loggerService: LoggerService
) : EnvironmentBuilder {
    override fun build(context: DotnetBuildContext): Disposable {
        val fallbackToStdOutTestReporting = TestReportingViaFileStreamingHelper.shouldFallbackToStdOutTestReporting(_parametersService)

        if (fallbackToStdOutTestReporting) return emptyDisposable()

        val testReportFilesPath = TestReportingViaFileStreamingHelper.getTestReportsFilesPath(_pathsService).toString()

        val serviceMessageFilePattern = testReportFilesPath + File.separator + "*.msg"
        val fileStreamingServiceMessage = FileStreamingServiceMessage(
            filePath = null,
            filePattern = serviceMessageFilePattern,
            wrapFileContentInBlock = false
        )
        _loggerService.writeMessage(fileStreamingServiceMessage)

        return emptyDisposable()
    }
}
