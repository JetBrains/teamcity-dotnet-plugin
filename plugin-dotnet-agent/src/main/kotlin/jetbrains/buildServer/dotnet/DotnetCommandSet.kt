/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Observer
import java.io.File

class DotnetCommandSet(
        private val _parametersService: ParametersService,
        commands: List<DotnetCommand>)
    : CommandSet {

    private val _knownCommands: Map<String, DotnetCommand> = commands.associateBy({ it.commandType.id }, { it })

    override val commands: Sequence<DotnetCommand>
        get() = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)?.let {
            _knownCommands[it]?.let { getCommands(it) }
        } ?: emptySequence()

    private fun getCommands(command: DotnetCommand): Sequence<CompositeCommand> =
            command.targetArguments.ifEmpty { sequenceOf(TargetArguments(emptySequence())) }.map { CompositeCommand(command.commandType.id, command, it) }

    class CompositeCommand(
            private val commandId: String,
            private val _command: DotnetCommand,
            private val _targetArguments: TargetArguments)
        : DotnetCommand by _command {

        override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
                sequence {
                    if (_command.toolResolver.isCommandRequired) {
                        // command
                        yieldAll(commandId.split('-')
                                .filter { it.isNotEmpty() }
                                .map { CommandLineArgument(it,  CommandLineArgumentType.Mandatory) })
                    }

                    // projects
                    yieldAll(_targetArguments.arguments)

                    var newContext = DotnetBuildContext(context.workingDirectory, this@CompositeCommand, context.toolVersion, context.verbosityLevel)

                    // command specific arguments
                    yieldAll(_command.getArguments(newContext))
                }

        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_targetArguments)
    }
}