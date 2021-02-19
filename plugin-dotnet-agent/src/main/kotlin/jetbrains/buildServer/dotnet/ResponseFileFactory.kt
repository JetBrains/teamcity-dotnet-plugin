package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path

interface ResponseFileFactory {
    fun createResponeFile(description: String, arguments: Collection<CommandLineArgument>, verbosity: Verbosity? = null): Path
}