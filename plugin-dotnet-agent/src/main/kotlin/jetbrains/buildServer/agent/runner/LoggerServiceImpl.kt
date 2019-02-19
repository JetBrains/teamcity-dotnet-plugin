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

    override fun writeStandardOutput(text: String, color: Color) =
            listener.onStandardOutput(applyColor(text, color))

    override fun writeStandardOutput(vararg text: Pair<String, Color>) =
            listener.onStandardOutput(applyColor(*text))

    override fun writeErrorOutput(text: String) = listener.onErrorOutput(text)

    override fun writeBlock(blockName: String, description: String): Closeable {
        buildLogger.message(BlockOpened(blockName, if (description.isBlank()) null else description).toString())
        return Closeable { buildLogger.message(BlockClosed(blockName).toString()) }
    }

    private fun applyColor(text: String, color: Color, prevColor: Color = Color.Default): String =
        if (color == Color.Default)
            if (color != prevColor)
                "\u001B[0m$text"
            else
                text
        else "\u001B[${_colorTheme.getAnsiColor(color)}m$text"

    private fun applyColor(vararg text: Pair<String, Color>): String =
            text.fold(Pair<String, Color>("", Color.Default)) {
                acc, (str, color) -> Pair<String, Color>(acc.first + applyColor(str, color, acc.second), color)
            }.first
}