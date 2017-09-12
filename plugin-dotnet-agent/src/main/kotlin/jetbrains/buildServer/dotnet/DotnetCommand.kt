package jetbrains.buildServer.dotnet

interface DotnetCommand: ArgumentsProvider {
    val commandType: DotnetCommandType

    val toolResolver: ToolResolver

    val targetArguments: Sequence<TargetArguments>

    fun isSuccessfulExitCode(exitCode: Int): Boolean
}