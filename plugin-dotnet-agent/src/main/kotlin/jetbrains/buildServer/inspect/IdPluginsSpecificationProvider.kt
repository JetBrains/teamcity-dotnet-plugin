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
import jetbrains.buildServer.agent.Version

class IdPluginsSpecificationProvider(
    private val _pluginDescriptorsProvider: PluginDescriptorsProvider,
    private val _loggerService: LoggerService,
    xmlElementGenerators: List<PluginXmlElementGenerator>
) : PluginsSpecificationProvider {
    private val _supportedSourceIds = xmlElementGenerators.map { it.sourceId }

    override fun getPluginsSpecification(): String? {
        val pluginIds = _pluginDescriptorsProvider.getPluginDescriptors().mapNotNull {
            if (PluginDescriptorType.ID == it.type) {
                return@mapNotNull it.value
            }

            logInvalidDescriptor(it)
            return@mapNotNull null
        }

        return when {
            pluginIds.isNotEmpty() -> pluginIds.joinToString(";")
            else -> null
        }
    }

    private fun logInvalidDescriptor(pluginDescriptor: PluginDescriptor) = _loggerService.writeWarning(
        "Invalid R# CLT plugin descriptor: \"${pluginDescriptor.value}\", " +
                "R# CLT ${Version.FirstInspectCodeWithExtensionsOptionVersion} and above does not support " +
                "$_supportedSourceIds descriptors, it will be ignored. Only plugin ID descriptor is supported."
    )
}