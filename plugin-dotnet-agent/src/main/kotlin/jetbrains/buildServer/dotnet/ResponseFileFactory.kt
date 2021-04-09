package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path

interface ResponseFileFactory {
    fun createResponeFile(
            description: String,
            arguments: Sequence<CommandLineArgument>,
            parameters: Sequence<MSBuildParameter>,
            verbosity: Verbosity? = null): Path
}