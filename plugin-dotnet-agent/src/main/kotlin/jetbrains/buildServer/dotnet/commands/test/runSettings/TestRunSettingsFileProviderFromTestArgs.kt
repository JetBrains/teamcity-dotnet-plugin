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

package jetbrains.buildServer.dotnet.commands.test.runSettings

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import java.io.File

class TestRunSettingsFileProviderFromTestArgs(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : TestRunSettingsFileProvider {

    override fun tryGet(command: DotnetCommandType) =
        command.takeIf { it == DotnetCommandType.Test }
                ?.let {
                    _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS)
                            ?.trim()
                            ?.let { _argumentsService.split(it) }
                            ?.filter { it.isNotBlank() }
                            ?.map { it.trim() }
                            ?.toList()
                            ?.let { args ->
                                args
                                        .indexOfLast { "--settings".equals(it, true) || "-s".equals(it, true) }
                                        .takeIf { it >= 0 && it < args.size - 1 }
                                        ?.let { args[it + 1] }
                                        ?.let { File(it) }
                            }
                }
}