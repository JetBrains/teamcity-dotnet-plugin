package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.agent.CommandLineArgument

data class OptionArguments(val arguments: Sequence<CommandLineArgument>)