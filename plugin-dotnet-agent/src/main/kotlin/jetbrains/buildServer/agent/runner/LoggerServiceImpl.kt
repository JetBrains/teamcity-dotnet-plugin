package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.Closeable

class LoggerServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _colorTheme: ColorTheme)
    : LoggerService {

    private val listener: LoggingProcessListener
        get() = LoggingProcessListener(buildLogger)

    private val buildLogger: BuildProgressLogger
        get() = _buildStepContext.runnerContext.build.buildLogger

    override fun writeMessage(serviceMessage: ServiceMessage) = buildLogger.message(serviceMessage.toString())

    override fun writeBuildProblem(buildProblem: BuildProblemData) = buildLogger.logBuildProblem(buildProblem)

    override fun writeStandardOutput(text: String, color: Color) {
        if (color == Color.Default) {
            listener.onStandardOutput(text)
        } else {
            listener.onStandardOutput("\u001B[${_colorTheme.getAnsiColor(color)}m$text")
        }
    }

    override fun writeErrorOutput(text: String) = listener.onErrorOutput(text)

    override fun writeBlock(blockName: String, description: String): Closeable {
        buildLogger.message(BlockOpened(blockName, if (description.isBlank()) null else description).toString())
        return Closeable { buildLogger.message(BlockClosed(blockName).toString()) }
    }
}