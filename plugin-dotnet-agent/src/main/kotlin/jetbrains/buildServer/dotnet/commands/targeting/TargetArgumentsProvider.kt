

package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTarget

interface TargetArgumentsProvider {
    fun getTargetArguments(targets: Sequence<CommandTarget>): Sequence<TargetArguments>
}