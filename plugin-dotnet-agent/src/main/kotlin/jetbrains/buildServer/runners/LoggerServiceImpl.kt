package jetbrains.buildServer.runners

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.runner.LoggingProcessListener
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class LoggerServiceImpl(
        private val _buildStepContext: BuildStepContext)
    : LoggerService {

    private val listener: LoggingProcessListener
        get() = LoggingProcessListener(buildLogger)

    private val buildLogger: BuildProgressLogger
        get() = _buildStepContext.runnerContext.getBuild().getBuildLogger()

    override fun onMessage(serviceMessage: ServiceMessage) = buildLogger.message(serviceMessage.toString())

    override fun onBuildProblem(buildProblem: BuildProblemData) = buildLogger.logBuildProblem(buildProblem)

    override fun onStandardOutput(text: String) = listener.onStandardOutput(text)

    override fun onErrorOutput(text: String) = listener.onErrorOutput(text)
}