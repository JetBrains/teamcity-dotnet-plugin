package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.TargetArguments
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage

// Decomposes one `dotnet` command applied to multiple targets
// For example `dotnet test` applied to `Abc.csproj`, `Bcd.csproj` and `Cde.csproj` will be decomposed to 3 commands:
// `dotnet test Abc.csproj`
// `dotnet test Bcd.csproj`
class MultiTargetDotnetCommandStreamResolver : DotnetCommandStreamResolverBase() {
    override val stage = DotnetCommandsStreamResolvingStage.Targeting

    override fun shouldBeApplied(commands: DotnetCommandsStream) =
        commands.any { it.targetArguments.count() > 1 }

    override fun apply(commands: DotnetCommandsStream) =
        commands
            .flatMap { originalCommand ->
                originalCommand
                    .targetArguments
                    .ifEmpty { sequenceOf(TargetArguments(emptySequence())) }
                    .map { SpecificTargetDotnetCommand(originalCommand, it) }
            }

    final class SpecificTargetDotnetCommand constructor(
        private val _originalCommonCommand: DotnetCommand,
        private val _specificTargetArguments: TargetArguments
    ) : DotnetCommand by _originalCommonCommand {
        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_specificTargetArguments)
    }
}
