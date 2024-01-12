

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProvider

class ArgumentsProviderStub(private val _arguments: Sequence<CommandLineArgument> = emptySequence())
    : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetCommandContext) = _arguments
}