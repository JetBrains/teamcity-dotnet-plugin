

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

/**
 * Provides arguments to the utility.
 */
interface ArgumentsProvider {
    fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument>
}