package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import java.io.File

interface DotnetCommand: ArgumentsProvider {
    val commandType: DotnetCommandType

    val toolResolver: ToolResolver

    val targetArguments: Sequence<TargetArguments>

    fun isSuccessfulExitCode(exitCode: Int): Boolean
}