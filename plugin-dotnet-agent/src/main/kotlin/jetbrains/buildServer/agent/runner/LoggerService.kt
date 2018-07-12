package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import java.io.Closeable

interface LoggerService {
    fun writeMessage(serviceMessage: ServiceMessage)

    fun writeBuildProblem(buildProblem: BuildProblemData)

    fun writeStandardOutput(text: String, color: Color = Color.Default)

    fun writeErrorOutput(text: String)

    fun writeBlock(blockName: String, description: String = ""): Closeable
}