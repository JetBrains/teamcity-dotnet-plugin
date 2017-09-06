package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommonArgumentsProvider
import jetbrains.buildServer.runners.CommandLineArgument

class DotnetCommonArgumentsProviderStub(override val arguments: Sequence<CommandLineArgument> = emptySequence()) : DotnetCommonArgumentsProvider {
}