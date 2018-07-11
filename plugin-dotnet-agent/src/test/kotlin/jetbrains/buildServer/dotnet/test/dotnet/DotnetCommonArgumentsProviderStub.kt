package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProvider

class DotnetCommonArgumentsProviderStub(private val _arguments: Sequence<CommandLineArgument> = emptySequence())
    : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext) = _arguments
}