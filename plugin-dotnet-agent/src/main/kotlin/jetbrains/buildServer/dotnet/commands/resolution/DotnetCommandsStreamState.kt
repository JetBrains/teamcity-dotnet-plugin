package jetbrains.buildServer.dotnet.commands.resolution

import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.ToolState

open class DotnetCommandsStreamState(
    val commands: Sequence<DotnetCommand>
)