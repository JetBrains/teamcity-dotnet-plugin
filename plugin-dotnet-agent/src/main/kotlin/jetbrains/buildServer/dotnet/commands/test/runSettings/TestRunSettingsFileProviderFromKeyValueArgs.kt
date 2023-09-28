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
import jetbrains.buildServer.dotnet.DotnetBuildContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import java.io.File

class TestRunSettingsFileProviderFromKeyValueArgs(
    _args: List<String>,
    private val _commands: List<DotnetCommandType>,
    private val _parametersService: ParametersService,
    private val _argumentsService: ArgumentsService)
    : TestRunSettingsFileProvider {

    private var _argRegex: Regex

    init {
        _argRegex = Regex("^(${_args.joinToString("|")})\\s*(.+)\\s*\$", RegexOption.IGNORE_CASE)
    }

    override fun tryGet(context: DotnetBuildContext) =
        context.command.commandType.takeIf { _commands.contains(it) }
            ?.let {
                _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS)
                    ?.trim()
                    ?.let { _argumentsService.split(it) }
                    ?.mapNotNull { _argRegex.matchEntire(it) }
                    ?.filter { it.groupValues.size == 3 }
                    ?.map { File(it.groupValues[2]) }
                    ?.lastOrNull()
            }
}