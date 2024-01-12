

package jetbrains.buildServer.dotnet.commands.transformation

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommand

class ComposedDotnetCommandTransformer : DotnetCommandsTransformer {
    override val stage: DotnetCommandsTransformationStage = DotnetCommandsTransformationStage.FinalComposition

    override fun shouldBeApplied(context: DotnetCommandContext, commands: DotnetCommandsStream) = true

    override fun apply(context: DotnetCommandContext, commands: DotnetCommandsStream) = commands.map { ComposedDotnetCommand(it) }

    class ComposedDotnetCommand(
        private val _originalCommand: DotnetCommand
    ) : DotnetCommand by _originalCommand {
        private val commandCommandLineArguments get() =
            when {
                toolResolver.isCommandRequired -> command.map { CommandLineArgument(it, CommandLineArgumentType.Mandatory) }
                else -> emptySequence()
            }

        override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
            // command
            yieldAll(commandCommandLineArguments)

            // targets e.g. project files or directories
            yieldAll(targetArguments.flatMap { it.arguments })

            // command specific arguments
            yieldAll(_originalCommand.getArguments(context))
        }
    }
}