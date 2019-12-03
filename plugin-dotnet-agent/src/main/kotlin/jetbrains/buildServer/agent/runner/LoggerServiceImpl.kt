package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.disposableOf

class LoggerServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _colorTheme: ColorTheme)
    : LoggerService {

    private val listener: LoggingProcessListener
        get() = LoggingProcessListener(_buildLogger)

    private val _buildLogger: BuildProgressLogger
        get() = _buildStepContext.runnerContext.build.buildLogger

    override fun writeMessage(serviceMessage: ServiceMessage) = _buildLogger.message(serviceMessage.toString())

    override fun writeBuildProblem(identity: String, type: String, description: String) =
            _buildLogger.logBuildProblem(BuildProblemData.createBuildProblem(identity.substring(0 .. Integer.min(identity.length, 60) - 1), type, description))

    override fun writeStandardOutput(text: String, color: Color) =
            listener.onStandardOutput(applyColor(text, color))

    override fun writeStandardOutput(vararg text: StdOutText) =
            listener.onStandardOutput(applyColor(*text))

    override fun writeErrorOutput(text: String) = _buildLogger.error(text)

    override fun writeWarning(text: String) = _buildLogger.warning(text)

    override fun writeBlock(blockName: String, description: String) = writeBlock(blockName, description, false)

    override fun writeTrace(text: String) =
        _buildLogger.logMessage(DefaultMessagesInfo.internalize(DefaultMessagesInfo.createTextMessage(text)))

    override fun writeTraceBlock(blockName: String, description: String) = writeBlock(blockName, description, true)

    private fun writeBlock(blockName: String, description: String, trace: Boolean): Disposable {
        val blockOpened = BlockOpened(blockName, if (description.isBlank()) null else description)
        if (trace) {
            blockOpened.addTag(DefaultMessagesInfo.TAG_INTERNAL)
        }

        _buildLogger.message(blockOpened.toString())
        return disposableOf { _buildLogger.message(BlockClosed(blockName).toString()) }
    }
    private fun applyColor(text: String, color: Color, prevColor: Color = Color.Default): String =
        if (color == prevColor) {
            text
        }
        else {
            if (color == Color.Default) {
                "\u001B[0m$text"
            } else {
                "\u001B[${_colorTheme.getAnsiColor(color)}m$text"
            }
        }

    private fun applyColor(vararg text: StdOutText): String =
            text.fold(DefaultStdOutText) {
                acc, (text, color) -> StdOutText(acc.text + applyColor(text, color, acc.color), color)
            }.text

    companion object {
        private val DefaultStdOutText = StdOutText("")
    }
}