package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

interface EnvironmentVariables {
    fun getVariables(context: DotnetBuildContext): Sequence<CommandLineEnvironmentVariable>
}