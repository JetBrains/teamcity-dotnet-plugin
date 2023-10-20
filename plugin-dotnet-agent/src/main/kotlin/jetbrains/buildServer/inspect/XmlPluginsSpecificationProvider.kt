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

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.ByteArrayOutputStream

class XmlPluginsSpecificationProvider(
    private val _pluginDescriptorsProvider: PluginDescriptorsProvider,
    private val _xmlWriter: XmlWriter,
    private val _loggerService: LoggerService,
    xmlElementGenerators: List<PluginXmlElementGenerator>
) : PluginsSpecificationProvider {
    private val _sourceIdToXmlElementGenerator = xmlElementGenerators.associateBy { it.sourceId.lowercase() }

    override fun getPluginsSpecification(): String? {
        val pluginXmlElements = _pluginDescriptorsProvider.getPluginDescriptors()
            .mapNotNull {
                if (PluginDescriptorType.SOURCE == it.type) {
                    val matchResult = PluginDescriptorType.SOURCE.regex.matchEntire(it.value)
                    if (matchResult != null) {
                        val sourceId = matchResult.groupValues[1]
                        val value = matchResult.groupValues[2]

                        val generator = _sourceIdToXmlElementGenerator[sourceId.lowercase()]
                        if (generator != null) {
                            return@mapNotNull generator.generateXmlElement(value)
                        }
                    }
                }

                logInvalidDescriptor(it)
                return@mapNotNull null
            }

        val pluginsXmlElement = XmlElement("Packages", pluginXmlElements.asSequence())
        if (!pluginsXmlElement.isEmpty) {
            ByteArrayOutputStream().use {
                _xmlWriter.write(pluginsXmlElement, it)
                return it.toString(Charsets.UTF_8.name())
            }
        }

        return null
    }

    private fun logInvalidDescriptor(pluginDescriptor: PluginDescriptor) = _loggerService.writeWarning(
        "Invalid R# CLT plugin descriptor: \"${pluginDescriptor.value}\", " +
                "R# CLT versions below ${Version.FirstInspectCodeWithExtensionsOptionVersion} support only " +
                "${_sourceIdToXmlElementGenerator.keys} descriptors, it will be ignored."
    )
}