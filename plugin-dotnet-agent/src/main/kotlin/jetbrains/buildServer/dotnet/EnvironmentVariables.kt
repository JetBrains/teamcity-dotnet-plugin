package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

interface EnvironmentVariables {
    fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable>
}