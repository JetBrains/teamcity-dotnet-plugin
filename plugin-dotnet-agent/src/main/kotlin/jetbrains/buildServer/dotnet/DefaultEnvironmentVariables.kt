package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineEnvironmentVariable

interface DefaultEnvironmentVariables {
    val variables: Sequence<CommandLineEnvironmentVariable>
}