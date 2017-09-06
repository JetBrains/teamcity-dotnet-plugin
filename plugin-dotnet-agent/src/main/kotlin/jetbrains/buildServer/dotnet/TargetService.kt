package jetbrains.buildServer.dotnet

interface TargetService {
    val targets: Sequence<CommandTarget>
}