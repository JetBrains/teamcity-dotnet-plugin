package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand

interface DotnetCommandsTransformer {
    val stage: DotnetCommandsTransformationStage

    fun shouldBeApplied(context: DotnetCommandContext, commands: DotnetCommandsStream): Boolean

    fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream): DotnetCommandsStream
}
typealias DotnetCommandsStream = Sequence<DotnetCommand>