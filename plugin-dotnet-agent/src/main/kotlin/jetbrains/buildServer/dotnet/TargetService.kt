package jetbrains.buildServer.dotnet

/***
 * Provides a list of target files for command.
 */
interface TargetService {
    val targets: Sequence<CommandTarget>
}