package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Disposable
import java.io.Closeable

interface LoggerService {
    fun writeMessage(serviceMessage: ServiceMessage)

    fun writeBuildProblem(buildProblem: BuildProblemData)

    fun writeStandardOutput(text: String, color: Color = Color.Default)

    fun writeStandardOutput(vararg text: Pair<String, Color>)

    fun writeErrorOutput(text: String)

    fun writeBlock(blockName: String, description: String = ""): Disposable
}