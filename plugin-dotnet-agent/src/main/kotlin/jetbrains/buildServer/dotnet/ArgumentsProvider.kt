package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

/**
 * Provides arguments to the utility.
 */
interface ArgumentsProvider {
    val arguments: Sequence<CommandLineArgument>
}
