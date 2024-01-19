package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.ExtensionsProvider
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.impl.dataImport.AsyncDataProcessingShutdownRunnable
import jetbrains.buildServer.agent.impl.dataImport.AsyncDataProcessingTask
import jetbrains.buildServer.agent.impl.dataImport.AsyncDataProcessor
import jetbrains.buildServer.agent.impl.serviceProcess.ServiceProcessManager
import jetbrains.buildServer.agent.impl.serviceProcess.ServiceProcessScope
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.disposableOf
import java.io.File

internal class TestCommandEnvironmentBuilder(
    private val _extensionsProvider: ExtensionsProvider,
    private val _buildStepContext: BuildStepContext,
    private val _serviceProcessManager: ServiceProcessManager,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
) : EnvironmentBuilder {
    override fun build(context: DotnetCommandContext): EnvironmentBuildResult {
        val fallbackToStdOutTestReporting = shouldFallbackToStdOutTestReporting(_parametersService)
        return if (fallbackToStdOutTestReporting)
            setupTestReportingViaStdOut() else
            setupTestReportingViaFiles(context.verbosityLevel)
    }

    private fun setupTestReportingViaStdOut(): EnvironmentBuildResult {
        val variables = sequenceOf(CommandLineEnvironmentVariable(FALLBACK_TO_STDOUT_TEST_REPORTING_ENV_VAR, "true"))
        return EnvironmentBuildResult(variables = variables)
    }

    private fun setupTestReportingViaFiles(verbosityLevel: Verbosity?): EnvironmentBuildResult {
        val testReportFilesPath = TestReportsFilesPathGenerator.getTestFilesPath(_pathsService).toString()
        val testReportsStreamingTask = setupTestReportFileStreaming(testReportFilesPath, verbosityLevel)

        val variables = sequenceOf(CommandLineEnvironmentVariable(TEAMCITY_TEST_REPORT_FILES_PATH_ENV_VAR, testReportFilesPath))
        val disposable = disposableOf { testReportsStreamingTask.shutdownGracefully() }
        return EnvironmentBuildResult(variables = variables, disposable = disposable)
    }

    private fun setupTestReportFileStreaming(testReportFilesPath: String, verbosityLevel: Verbosity?): AsyncDataProcessingTask {
        val serviceMessageFilePattern = testReportFilesPath + File.separator + "*.msg"

        val enableQuietModeForFileStreaming = when (verbosityLevel) {
            Verbosity.Detailed, Verbosity.Diagnostic -> false
            else -> true
        }

        val arguments = mutableMapOf(
            "filePattern" to serviceMessageFilePattern,
            "wrapFileContentInBlock" to "false",
            "quiet" to enableQuietModeForFileStreaming.toString()
        )

        return startDataProcessing(arguments)
    }

    private fun startDataProcessing(arguments: MutableMap<String, String>): AsyncDataProcessingTask {
        val fileStreamingDataProcessor = getFileStreamingProcessor()

        val processingContext = object : DataProcessorContext {
            override fun getBuild(): AgentRunningBuild = _buildStepContext.runnerContext.build
            override fun getFile(): File = throw NotImplementedError("No single file is used for test reports import")
            override fun getArguments(): MutableMap<String, String> = arguments
        }

        val testReportsStreamingTask = fileStreamingDataProcessor.startDataProcessing(processingContext)

        val gracefulShutdownRunnable = AsyncDataProcessingShutdownRunnable(
            testReportsStreamingTask,
            _buildStepContext.runnerContext.build.buildLogger,
            fileStreamingDataProcessor.type
        )

        _serviceProcessManager.registerRunningProcess(
            "dotnet-test-reporting-${fileStreamingDataProcessor.type}-processor",
            gracefulShutdownRunnable,
            ServiceProcessScope.BUILD_STEP
        )

        return testReportsStreamingTask
    }

    private fun getFileStreamingProcessor(): AsyncDataProcessor {
        val asyncDataProcessors = _extensionsProvider.getExtensions(AsyncDataProcessor::class.java)
        val fileStreamingDataProcessor = asyncDataProcessors.firstOrNull { it.type == FILE_STREAMING_PROCESSOR_TYPE }
        if (fileStreamingDataProcessor == null) {
            throw Exception("Could not find the '${FILE_STREAMING_PROCESSOR_TYPE}' processor to stream the test report files to the build log")
        }

        return fileStreamingDataProcessor
    }

    private fun shouldFallbackToStdOutTestReporting(parametersService: ParametersService): Boolean {
        val fallbackToStdOutTestReportingValue = parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING)
        return fallbackToStdOutTestReportingValue.toBoolean()
    }

    companion object {
        private const val FILE_STREAMING_PROCESSOR_TYPE = "streamToBuildLog"
        private const val FALLBACK_TO_STDOUT_TEST_REPORTING_ENV_VAR = "TEAMCITY_FALLBACK_TO_STDOUT_TEST_REPORTING"
        private const val TEAMCITY_TEST_REPORT_FILES_PATH_ENV_VAR = "TEAMCITY_TEST_REPORT_FILES_PATH"
    }
}
