package jetbrains.buildServer.dotnet.commands.resolution.resolvers

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandStreamResolverBase
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStream
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsStreamResolvingStage


class ComposedDotnetCommandStreamResolver : DotnetCommandStreamResolverBase() {
    override val stage: DotnetCommandsStreamResolvingStage = DotnetCommandsStreamResolvingStage.FinalComposition

    override fun shouldBeApplied(commands: DotnetCommandsStream) = true

    override fun apply(commands: DotnetCommandsStream) = commands.map { ComposedDotnetCommand(it) }

    final class ComposedDotnetCommand constructor(
        private val _originalCommonCommand: DotnetCommand
    ) : DotnetCommand by _originalCommonCommand {
        private val commandCommandLineArguments get() =
            when {
                toolResolver.isCommandRequired -> commandWords.map { CommandLineArgument(it, CommandLineArgumentType.Mandatory) }
                else -> emptySequence()
            }

        override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
            // command
            yieldAll(commandCommandLineArguments)

            // targets e.g. project files or directories
            yieldAll(targetArguments.flatMap { it.arguments })

            // command specific arguments
            yieldAll(_originalCommonCommand.getArguments(context))
        }
    }
}