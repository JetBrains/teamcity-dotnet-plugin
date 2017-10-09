package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

data class TargetArguments(val arguments: Sequence<CommandLineArgument>)