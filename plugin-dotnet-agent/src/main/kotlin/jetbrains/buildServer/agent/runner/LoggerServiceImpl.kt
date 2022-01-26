/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.messages.serviceMessages.BlockClosed
import jetbrains.buildServer.messages.serviceMessages.BlockOpened
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.disposableOf

class LoggerServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _buildInfo: BuildInfo,
        private val _colorTheme: ColorTheme)
    : LoggerService {

    private val listener: LoggingProcessListener
        get() = LoggingProcessListener(_buildLogger)

    private val _buildLogger: BuildProgressLogger
        get() = _buildStepContext.runnerContext.build.buildLogger

    override fun writeMessage(serviceMessage: ServiceMessage) = _buildLogger.message(serviceMessage.toString())

    override fun writeBuildProblem(identity: String, type: String, description: String) {
        var id = "${_buildInfo.id}:$identity"
        id = id.substring(0 .. Integer.min(id.length, 60) - 1)
        _buildLogger.logBuildProblem(BuildProblemData.createBuildProblem(id, type, "$description (Step: ${_buildInfo.name})"))
    }

    override fun writeStandardOutput(text: String, color: Color) =
            listener.onStandardOutput(applyColor(text, color))

    override fun writeStandardOutput(vararg text: StdOutText) =
            listener.onStandardOutput(applyColor(*text))

    override fun writeErrorOutput(text: String) = _buildLogger.error(text)

    override fun writeWarning(text: String) = _buildLogger.warning(text)

    override fun writeBlock(blockName: String, description: String) = writeBlock(blockName, description, false)

    override fun writeTrace(text: String) =
        _buildLogger.logMessage(DefaultMessagesInfo.internalize(DefaultMessagesInfo.createTextMessage(text)))

    override fun buildFailureDescription(description: String) = _buildLogger.buildFailureDescription(description)

    override fun writeTraceBlock(blockName: String, description: String) = writeBlock(blockName, description, true)

    override fun importData(dataProcessorType: String, artifactPath: Path, tool: String) = writeMessage(ImportDataServiceMessage(dataProcessorType, artifactPath, tool))

    private fun writeBlock(blockName: String, description: String, trace: Boolean): Disposable {
        val blockOpened = BlockOpened(blockName, description.ifBlank { null })
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