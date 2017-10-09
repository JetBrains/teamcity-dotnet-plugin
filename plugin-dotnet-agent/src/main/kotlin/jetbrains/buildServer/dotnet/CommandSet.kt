package jetbrains.buildServer.dotnet

interface CommandSet {
    val commands: Sequence<DotnetCommand>
}