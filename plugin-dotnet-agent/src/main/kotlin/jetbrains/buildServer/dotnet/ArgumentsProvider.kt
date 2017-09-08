package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument

/**
 * Provides arguments to the utility.
 */
interface ArgumentsProvider {
    val arguments: Sequence<CommandLineArgument>
}
