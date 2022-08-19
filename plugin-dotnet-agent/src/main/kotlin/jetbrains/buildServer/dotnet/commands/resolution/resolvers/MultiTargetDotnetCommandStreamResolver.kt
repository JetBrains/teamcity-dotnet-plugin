package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetBuildContext
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
                    .map { SpecificTargetDotnetCommand(originalCommand, sequenceOf(it)) }
            }

    final class SpecificTargetDotnetCommand constructor(
        private val _originalCommonCommand: DotnetCommand,
        override val targetArguments: Sequence<TargetArguments>
    ) : DotnetCommand by _originalCommonCommand {
//        override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
//            sequence {
//                if (_originalCommonCommand.toolResolver.isCommandRequired) {
//                    // command
//                    yieldAll(_originalCommonCommand.commandWords.map { CommandLineArgument(it, CommandLineArgumentType.Mandatory) })
//                }
//
//                // projects
//                yieldAll(_specificTargetArguments.arguments)
//
//                var newContext = context.deriveNewFor(this@SpecificTargetDotnetCommand)
//
//                // command specific arguments
//                yieldAll(_originalCommonCommand.getArguments(newContext))
//            }

//        override val targetArguments: Sequence<TargetArguments>
//            get() = sequenceOf(_specificTargetArguments)
    }
}
