package jetbrains.buildServer.runners

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

interface LoggerService {
    fun onMessage(serviceMessage: ServiceMessage)

    fun onBuildProblem(buildProblem: BuildProblemData)

    fun onStandardOutput(text: String)

    fun onErrorOutput(text: String)
}