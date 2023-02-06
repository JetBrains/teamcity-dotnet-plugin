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

package jetbrains.buildServer.dotnet.commands.targeting

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.CommandTargetType
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_SINGLE_SESSION
import java.io.File

class TargetArgumentsProviderByType(
        private val _parametersService: ParametersService,
        private val _targetTypeProvider: TargetTypeProvider
):
    TargetArgumentsProvider {

    override fun getTargetArguments(targets: Sequence<CommandTarget>) =
            if (isEnabled) splitByTargetType(targets) else splitByDefault(targets)

    private val isEnabled: Boolean
        get() = _parametersService.tryGetParameter(ParameterType.Runner, PARAM_SINGLE_SESSION)?.trim()?.toBoolean() ?: false

    private fun splitByTargetType(targets: Sequence<CommandTarget>): Sequence<TargetArguments> =
            targets
                    .groupBy { _targetTypeProvider.getTargetType(File(it.target.path)) }
                    .asSequence()
                    .map {
                        when (it.key) {
                            CommandTargetType.Assembly -> sequenceOf(TargetArguments(it.value.map { CommandLineArgument(it.target.path, CommandLineArgumentType.Target) }.asSequence()))
                            else -> splitByDefault(it.value.asSequence())
                        }
                    }.flatMap { it }

    private fun splitByDefault(targets: Sequence<CommandTarget>) =
            targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }
}