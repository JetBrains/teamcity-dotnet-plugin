package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.serviceMessages.FileStreamingServiceMessage
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.util.EventDispatcher
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

// Class is not thread-safe – it is assumed that environment setup or agent event methods are never called concurrently
internal class BuildStepScopedTestEnvironmentBuilderImpl(
    agentEventDispatcher: EventDispatcher<AgentLifeCycleListener>,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _loggerService: LoggerService) : AgentLifeCycleAdapter(), BuildStepScopedTestEnvironmentBuilder {

    private val uniquePathPartGenerator = UniquePathPartGenerator()
    private var wasEnvironmentSetupForCurrentBuildStep = false
    private var uniqueIdForCurrentBuildStep: String? = null

    init {
        agentEventDispatcher.addListener(this)
    }

    override fun setupEnvironmentForTestReporting(verbosityLevel: Verbosity?) {
        val fallbackToStdOutTestReporting = TestReportingViaFileStreamingHelper.shouldFallbackToStdOutTestReporting(_parametersService)

        if (fallbackToStdOutTestReporting || wasEnvironmentSetupForCurrentBuildStep) return

        val testReportFilesPath = getTestReportsFilesPathForBuildStep().toString()

        val serviceMessageFilePattern = testReportFilesPath + File.separator + "*.msg"

        val enableQuietModeForFileStreaming = when (verbosityLevel) {
            Verbosity.Detailed, Verbosity.Diagnostic -> false
            else -> true
        }

        val fileStreamingServiceMessage = FileStreamingServiceMessage(
            filePath = null,
            filePattern = serviceMessageFilePattern,
            wrapFileContentInBlock = false,
            quietMode = enableQuietModeForFileStreaming
        )
        _loggerService.writeMessage(fileStreamingServiceMessage)

        wasEnvironmentSetupForCurrentBuildStep = true
    }

    override fun getTestReportsFilesPathForBuildStep(): Path {
        val agentTempPath = _pathsService.getPath(PathType.AgentTemp).canonicalPath
        return Paths.get(agentTempPath, "TestReports", getUniqueIdForBuildStep()).toAbsolutePath()
    }

    private fun getUniqueIdForBuildStep(): String {
        ensureUniqueIdForBuildStepIsSet()
        return uniqueIdForCurrentBuildStep!!
    }

    private fun ensureUniqueIdForBuildStepIsSet() {
        if (uniqueIdForCurrentBuildStep == null) {
            uniqueIdForCurrentBuildStep = uniquePathPartGenerator.generateUniquePathPart()
        }
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        wasEnvironmentSetupForCurrentBuildStep = false
        uniqueIdForCurrentBuildStep = null
    }

    internal class UniquePathPartGenerator
    {
        fun generateUniquePathPart(): String
        {
            val uuid = UUID.randomUUID()
            val byteArray = ByteBuffer.allocate(16)
                .putLong(uuid.mostSignificantBits)
                .putLong(uuid.leastSignificantBits)
                .array()
            return Base64.getUrlEncoder().encodeToString(byteArray)
        }
    }
}
