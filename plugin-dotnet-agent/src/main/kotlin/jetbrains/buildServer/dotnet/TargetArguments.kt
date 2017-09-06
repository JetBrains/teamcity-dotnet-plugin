package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument

data class TargetArguments(val arguments: Sequence<CommandLineArgument>) {
}