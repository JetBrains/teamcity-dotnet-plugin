package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProvider
import jetbrains.buildServer.agent.CommandLineArgument

class DotnetCommonArgumentsProviderStub(override val arguments: Sequence<CommandLineArgument> = emptySequence())
    : DotnetCommonArgumentsProvider