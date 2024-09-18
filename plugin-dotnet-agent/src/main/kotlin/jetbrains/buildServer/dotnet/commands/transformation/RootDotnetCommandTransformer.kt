package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetCommandContext

// The transformer that applies all other transformers in a proper order
class RootDotnetCommandTransformer(
    private val _dotnetCommandsTransformers: List<DotnetCommandsTransformer>,
) : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Initial

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        _dotnetCommandsTransformers
            .sortedBy { it.stage.ordinal }
            .fold(commands) { stream, transformer ->
                transformer.apply(context, stream)
            }
}