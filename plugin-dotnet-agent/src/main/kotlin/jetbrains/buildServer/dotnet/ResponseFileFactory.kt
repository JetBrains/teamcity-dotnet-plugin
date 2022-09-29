package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter

interface ResponseFileFactory {
    fun createResponeFile(
        description: String,
        arguments: Sequence<CommandLineArgument>,
        parameters: Sequence<MSBuildParameter>,
        verbosity: Verbosity? = null): Path
}