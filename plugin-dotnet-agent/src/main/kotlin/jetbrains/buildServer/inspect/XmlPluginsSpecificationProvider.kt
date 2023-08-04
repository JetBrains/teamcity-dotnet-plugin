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

import jetbrains.buildServer.DocElement
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.ByteArrayOutputStream

class XmlPluginsSpecificationProvider(
    private val _pluginParametersProvider: PluginParametersProvider,
    private val _xmlWriter: XmlWriter,
    private val _loggerService: LoggerService,
    sources: List<PluginSource>
) : PluginsSpecificationProvider {
    private val _sources: Map<String, PluginSource> = sources.associateBy { it.id.lowercase() }

    override fun getPluginsSpecification(): String? {
        val pluginElements = _pluginParametersProvider.getPluginParameters()
            .mapNotNull {
                val sourceId = it.source
                val source = _sources[sourceId.lowercase()]
                return@mapNotNull if (source != null) {
                    source.getPlugin(it.value)
                } else {
                    _loggerService.writeWarning("Unrecognized plugin source: $sourceId, current configuration supports only ${_sources.keys}")
                    null
                }
            }

        val pluginsDocElement = DocElement("Packages", pluginElements.asSequence())
        if (!pluginsDocElement.isEmpty) {
            ByteArrayOutputStream().use {
                _xmlWriter.write(pluginsDocElement, it)
                return it.toString(Charsets.UTF_8.name())
            }
        }

        return null
    }
}