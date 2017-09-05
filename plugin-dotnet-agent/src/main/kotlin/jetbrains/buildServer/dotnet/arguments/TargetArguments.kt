package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.runners.CommandLineArgument

data class TargetArguments(val arguments: Sequence<CommandLineArgument>) {
}