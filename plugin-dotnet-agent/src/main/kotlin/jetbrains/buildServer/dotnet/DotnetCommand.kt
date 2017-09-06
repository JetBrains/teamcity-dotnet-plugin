package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument

interface DotnetCommand {
    val commandType: DotnetCommandType

    val targetArguments: Sequence<TargetArguments>

    val specificArguments: Sequence<CommandLineArgument>

    fun isSuccess(exitCode: Int): Boolean
}