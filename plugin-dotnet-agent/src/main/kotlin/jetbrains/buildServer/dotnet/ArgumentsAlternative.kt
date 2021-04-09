package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

interface ArgumentsAlternative {
    fun select(description: String, arguments: Collection<CommandLineArgument>, alternativeArguments: Sequence<CommandLineArgument>, alternativeParameters: Sequence<MSBuildParameter>, verbosity: Verbosity? = null): Sequence<CommandLineArgument>
}