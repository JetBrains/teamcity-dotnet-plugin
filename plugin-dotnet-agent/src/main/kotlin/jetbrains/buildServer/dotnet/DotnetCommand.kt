package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult

interface DotnetCommand: ArgumentsProvider {
    val commandType: DotnetCommandType

    val toolResolver: ToolResolver

    val targetArguments: Sequence<TargetArguments>

    fun isSuccessful(result: CommandLineResult): Boolean
}