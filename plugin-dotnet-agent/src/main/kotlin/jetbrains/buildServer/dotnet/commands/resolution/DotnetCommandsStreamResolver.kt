package jetbrains.buildServer.dotnet.commands.resolution

import jetbrains.buildServer.dotnet.DotnetCommand

interface DotnetCommandsStreamResolver {
    val stage: DotnetCommandsStreamResolvingStage
    fun resolve(commands: DotnetCommandsStream = emptySequence()): DotnetCommandsStream
}

typealias DotnetCommandsStream = Sequence<DotnetCommand>