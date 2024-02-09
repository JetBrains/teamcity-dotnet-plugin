

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Disposable

interface LoggerService {
    fun writeMessage(serviceMessage: ServiceMessage)

    fun writeBuildProblem(identity: String, type: String, description: String)

    fun writeStandardOutput(text: String, color: Color = Color.Default)

    fun writeStandardOutput(vararg text: StdOutText)

    fun writeErrorOutput(text: String)

    fun writeWarning(text: String)

    fun writeDebug(text: String)

    fun writeBlock(blockName: String, description: String = ""): Disposable

    fun writeTrace(text: String)

    fun writeTraceBlock(blockName: String, description: String = ""): Disposable

    fun buildFailureDescription(description: String)

    fun importData(dataProcessorType: String, artifactPath: Path, tool: String = "")
}