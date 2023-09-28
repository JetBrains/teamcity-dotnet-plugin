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

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import java.io.File

class TestRunSettingsFileProviderFromParams(
        private val _parametersService: ParametersService)
    : TestRunSettingsFileProvider {

    override fun tryGet(context: DotnetCommandContext) =
        context.command.commandType.takeIf { it == DotnetCommandType.Test || it == DotnetCommandType.VSTest }
                    ?.let {
                        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_SETTINGS_FILE)
                                ?.let { it.ifBlank { null } }
                                ?.let { File(it) }
                    }
}