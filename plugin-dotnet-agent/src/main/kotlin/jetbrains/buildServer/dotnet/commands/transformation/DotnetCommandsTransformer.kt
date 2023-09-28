package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommand

interface DotnetCommandsTransformer {
    val stage: DotnetCommandsTransformationStage

    fun shouldBeApplied(context: DotnetBuildContext, commands: DotnetCommandsStream): Boolean

    fun apply(context: DotnetBuildContext, commands: DotnetCommandsStream): DotnetCommandsStream
}
typealias DotnetCommandsStream = Sequence<DotnetCommand>