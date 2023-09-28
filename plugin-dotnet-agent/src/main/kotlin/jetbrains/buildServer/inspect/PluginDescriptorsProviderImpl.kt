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

class PluginDescriptorsProviderImpl(
    private val _parametersService: ParametersService,
    private val _loggerService: LoggerService
) : PluginDescriptorsProvider {

    override fun getPluginDescriptors(): List<PluginDescriptor> {
        return getPluginLines()
            .mapNotNull { line ->
                PluginDescriptorType.values().forEach {
                    val pluginDescriptor = tryCreateDescriptorOfType(line, it)
                    if (pluginDescriptor != null) return@mapNotNull pluginDescriptor
                }

                _loggerService.writeWarning("Invalid R# CLT plugin descriptor: \"$line\", it will be ignored.")
                return@mapNotNull null
            }
    }

    override fun hasPluginDescriptors(): Boolean {
        return getPluginLines().isNotEmpty()
    }

    private fun getPluginLines(): List<String> {
        val plugins = _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS)
        if (!plugins.isNullOrBlank()) {
            return plugins.lines()
                .filter { it.isNotBlank() }
                .map { it.trim() }
                .toList()
        }

        return emptyList()
    }

    private fun tryCreateDescriptorOfType(value: String, type: PluginDescriptorType): PluginDescriptor? {
        val typeMatch = type.regex.matchEntire(value)
        return if (typeMatch != null) PluginDescriptor(type, value) else null
    }
}