package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

interface EnvironmentVariables {
    val variables: Sequence<CommandLineEnvironmentVariable>
}