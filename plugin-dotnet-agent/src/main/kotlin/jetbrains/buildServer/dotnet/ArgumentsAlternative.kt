package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

interface ArgumentsAlternative {
    fun select(description: String, arguments: Collection<CommandLineArgument>, parameters: Sequence<MSBuildParameter>, verbosity: Verbosity? = null): Sequence<CommandLineArgument>
}