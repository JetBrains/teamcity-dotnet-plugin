/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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