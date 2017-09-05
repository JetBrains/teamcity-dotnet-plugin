package jetbrains.buildServer.dotnet.arguments

interface TargetService {
    val targets: Sequence<CommandTarget>
}