package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetBuildContext

// The transformer that applies all other transformers in a proper order
class RootDotnetCommandTransformer(
    private val _dotnetCommandsTransformers: List<DotnetCommandsTransformer>,
) : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Initial

    override fun shouldBeApplied(context: DotnetBuildContext, commands: DotnetCommandsStream) = true

    override fun apply(context: DotnetBuildContext, commands: DotnetCommandsStream) =
        _dotnetCommandsTransformers
            .sortedBy { it.stage.ordinal }
            .fold(commands) { stream, transformer ->
                when {
                    transformer.shouldBeApplied(context, stream) -> transformer.apply(context, stream)
                    else -> stream
                }
            }
}