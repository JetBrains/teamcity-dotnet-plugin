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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.common.MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR

class InspectCodeEnvironmentProvider(
    private val _pluginsSpecificationProvider: PluginsSpecificationProvider,
    private val _parametersService: ParametersService
) : EnvironmentProvider {
    override fun getEnvironmentVariables(toolVersion: Version, toolPlatform: InspectionToolPlatform) = sequence {
        if (toolVersion < Version.FirstInspectCodeWithExtensionsOptionVersion) {
            _pluginsSpecificationProvider.getPluginsSpecification()?.let {
                yield(CommandLineEnvironmentVariable(PLUGIN_LIST_ENV_VAR, it))
            }
        }

        // shared compilation on Windows spawns VBCSCompiler that locks files in buildTmp, disable it by default
        if (toolPlatform != InspectionToolPlatform.CrossPlatform && _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR).isNullOrBlank()) {
            yield(CommandLineEnvironmentVariable(USE_SHARED_COMPILATION_ENV_VAR, "false"))
        }
    }

    companion object {
        internal const val PLUGIN_LIST_ENV_VAR = "JET_ADDITIONAL_DEPLOYED_PACKAGES"
    }
}