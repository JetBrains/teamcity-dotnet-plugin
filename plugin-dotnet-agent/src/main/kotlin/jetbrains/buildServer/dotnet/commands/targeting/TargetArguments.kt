

package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.agent.CommandLineArgument

data class TargetArguments(val arguments: Sequence<CommandLineArgument>)