package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetCommandContext

interface DotnetCommandsTransformer {
    val stage: DotnetCommandsTransformationStage

    fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream
}
typealias DotnetCommandsStream = Sequence<DotnetCommand>