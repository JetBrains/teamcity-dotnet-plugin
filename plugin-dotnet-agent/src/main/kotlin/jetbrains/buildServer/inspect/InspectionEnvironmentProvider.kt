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
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS
import java.io.ByteArrayOutputStream

class InspectionEnvironmentProvider(
        private val _parametersService: ParametersService,
        private val _pluginsListProvider: PackagesProvider,
        private val _xmlWriter: XmlWriter)
    : EnvironmentProvider {
    override fun getEnvironmentVariables() = sequence {
        _parametersService.tryGetParameter(ParameterType.Runner, RUNNER_SETTING_CLT_PLUGINS)?.let {
            var plugins = _pluginsListProvider.getPackages(it)
            if (!plugins.isEmpty) {
                ByteArrayOutputStream().use {
                    _xmlWriter.write(plugins, it)
                    yield(CommandLineEnvironmentVariable(PluginListEnvVar, it.toString(Charsets.UTF_8.name())))
                }
            }
        }
    }

    companion object {
        internal const val PluginListEnvVar = "JET_ADDITIONAL_DEPLOYED_PACKAGES"
    }
}