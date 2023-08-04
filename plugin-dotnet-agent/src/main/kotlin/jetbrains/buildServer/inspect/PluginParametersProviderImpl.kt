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

import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class PluginParametersProviderImpl(
    private val _parametersService: ParametersService,
    private val _loggerService: LoggerService
) : PluginParametersProvider {

    override fun getPluginParameters(): List<PluginParameter> {
        val plugins = _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS)
        if (!plugins.isNullOrBlank()) {
            return plugins.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull {
                    val match = PLUGIN_LINE_REGEX.matchEntire(it)
                    return@mapNotNull if (match != null) {
                        PluginParameter(match.groupValues[1], match.groupValues[2])
                    } else {
                        _loggerService.writeWarning("Invalid R# plugin specification: $it")
                        null
                    }
                }
        }

        return emptyList()
    }

    override fun hasPluginParameters(): Boolean {
        val plugins = _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS)
        return !plugins.isNullOrBlank()
    }

    companion object {
        private val PLUGIN_LINE_REGEX = Regex("^(\\S+)\\s+(.*)$")
    }
}