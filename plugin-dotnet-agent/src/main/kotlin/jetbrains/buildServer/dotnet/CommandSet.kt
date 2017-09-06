package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.ArgumentsProvider

interface CommandSet {
    val commands: Sequence<DotnetCommand>
}