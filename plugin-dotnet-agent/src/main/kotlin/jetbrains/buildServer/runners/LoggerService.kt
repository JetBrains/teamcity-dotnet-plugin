package jetbrains.buildServer.runners

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import org.jetbrains.annotations.NotNull
import java.io.Closeable

interface LoggerService {
    fun onMessage(serviceMessage: ServiceMessage)

    fun onBuildProblem(buildProblem: BuildProblemData)

    fun onStandardOutput(text: String, color: Color = Color.Default)

    fun onErrorOutput(text: String)

    fun onBlock(blockName: String, description: String = ""): Closeable
}