package jetbrains.buildServer.dotnet

interface DotnetCommand : ArgumentsProvider {
    val targetArguments: Sequence<TargetArguments>

    val commandType: DotnetCommandType

    fun isSuccess(exitCode: Int): Boolean
}