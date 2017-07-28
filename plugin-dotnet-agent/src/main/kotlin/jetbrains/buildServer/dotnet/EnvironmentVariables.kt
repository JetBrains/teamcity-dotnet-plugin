package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineEnvironmentVariable

interface EnvironmentVariables {
    val variables: Sequence<CommandLineEnvironmentVariable>
}