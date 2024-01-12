

package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.dotnet.CommandTarget

/***
 * Provides a list of target files for command.
 */
interface TargetService {
    val targets: Sequence<CommandTarget>
}