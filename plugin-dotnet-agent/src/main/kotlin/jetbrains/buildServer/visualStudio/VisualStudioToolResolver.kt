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

package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_VERSION
import jetbrains.buildServer.dotnet.Tool
import java.io.File

class VisualStudioToolResolver(private val _parametersService: ParametersService)
    : ToolResolver {
    override val executableFile: File
        get() {
            val paramName = "VS${version}_Path"
            val path = _parametersService.tryGetParameter(ParameterType.Configuration, paramName)
                    ?: throw RunBuildException("Can't find configuration parameter \"$paramName\"")
            return File(path, VSToolName)
        }

    private val version: Int
        get() =
            selectedVersion ?: availableVersions.sortedByDescending { it }.firstOrNull()
            ?: throw RunBuildException("Can't find any version of visual studio")

    private val selectedVersion: Int?
        get() {
            val version =  _parametersService.tryGetParameter(ParameterType.Runner, PARAM_VISUAL_STUDIO_VERSION)?.let {
                Tool.tryParse(it)?.vsVersion
                        ?: throw RunBuildException("Can't parse visual studio version from \"$PARAM_VISUAL_STUDIO_VERSION\" value \"$it\"")
            }

            return if (version != null && version != Tool.VisualStudioAny.version) version else null
        }

    private val availableVersions: Sequence<Int>
        get() =
            _parametersService.getParameterNames(ParameterType.Configuration)
                    .map { VSConfigNamePattern.find(it) }
                    .filter { it != null }
                    .map { it!!.groupValues[1].toIntOrNull() }
                    .filter { it != null }
                    .map { it as Int }

    companion object {
        private val VSConfigNamePattern: Regex = Regex("VS(\\d+)_Path", option = RegexOption.IGNORE_CASE)
        const val VSToolName = "devenv.com"
    }
}