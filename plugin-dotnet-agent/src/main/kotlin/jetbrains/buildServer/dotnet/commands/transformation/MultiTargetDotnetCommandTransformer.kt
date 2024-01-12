

package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments

// Decomposes one `dotnet` command applied to multiple targets
// For example `dotnet test` applied to `Abc.csproj`, `Bcd.csproj` and `Cde.csproj` will be decomposed to 3 commands:
// `dotnet test Abc.csproj`
// `dotnet test Bcd.csproj`
// `dotnet test Cde.csproj`
class MultiTargetDotnetCommandTransformer : DotnetCommandsTransformer {
    override val stage = DotnetCommandsTransformationStage.Targeting

    override fun shouldBeApplied(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        commands.any { it.targetArguments.count() > 1 }

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream) =
        commands
            .flatMap { originalCommand ->
                originalCommand
                    .targetArguments
                    .ifEmpty { sequenceOf(TargetArguments(emptySequence())) }
                    .map { SpecificTargetDotnetCommand(originalCommand, it) }
            }

    class SpecificTargetDotnetCommand(
        private val _originalCommand: DotnetCommand,
        private val _specificTargetArguments: TargetArguments
    ) : DotnetCommand by _originalCommand {
        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_specificTargetArguments)
    }
}