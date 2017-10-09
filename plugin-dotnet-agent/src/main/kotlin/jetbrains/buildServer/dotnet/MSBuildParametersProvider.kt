package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

interface MSBuildParametersProvider {
    val parameters: Sequence<MSBuildParameter>
}