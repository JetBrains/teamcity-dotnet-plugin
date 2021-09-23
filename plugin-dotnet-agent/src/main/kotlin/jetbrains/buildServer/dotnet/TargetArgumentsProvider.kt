package jetbrains.buildServer.dotnet

interface TargetArgumentsProvider {
    fun getTargetArguments(targets: Sequence<CommandTarget>): Sequence<TargetArguments>
}