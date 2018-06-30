package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProvider

class DotnetCommonArgumentsProviderStub(override val arguments: Sequence<CommandLineArgument> = emptySequence())
    : DotnetCommonArgumentsProvider