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

package jetbrains.buildServer.agent

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import jetbrains.buildServer.rx.use
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PropertiesExtension(
    private val _dispatcher: CoroutineDispatcher,
    private val _agentPropertiesProviders: List<AgentPropertiesProvider>,
    extensionHolder: ExtensionHolder
) : AgentParametersSupplier {
    private val _lockObject = Object()
    private val dotNetParameters by lazy {
        val parameters = mutableMapOf<String, String>()
        LOG.infoBlock("Fetched agent properties").use {
            runBlocking {
                _agentPropertiesProviders.map { agentPropertiesProvider ->
                    launch(_dispatcher){
                        fetchProperties(agentPropertiesProvider, parameters)
                    }
                }.joinAll()
                parameters
            }
        }
    }

    init {
        extensionHolder.registerExtension(AgentParametersSupplier::class.java, PROPERTIES_EXTENSION_NAME, this)
    }

    override fun getParameters(): MutableMap<String, String> = dotNetParameters

    private fun fetchProperties(agentPropertiesProvider: AgentPropertiesProvider, parameters: MutableMap<String, String>) {
        LOG.debugBlock("Fetching agent properties for ${agentPropertiesProvider.desription}").use {
            try {
                for (property in agentPropertiesProvider.properties) {
                    var name = property.name
                    synchronized(_lockObject) {
                        val prevValue = parameters.get(name)
                        if (prevValue != null) {
                            LOG.warn("Update ${name}=\"${property.value}\". Previous value was \"$prevValue\".")
                        } else {
                            LOG.info("${name}=\"${property.value}\".")
                        }

                        parameters.put(name, property.value)
                    }
                }
            } catch (e: Exception) {
                LOG.debug("Error while fetching the agent properties for ${agentPropertiesProvider.desription}", e)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(PropertiesExtension::class.java)
        // This constant is used in the FxCop plugin
        const val PROPERTIES_EXTENSION_NAME = "DotNetPropertiesExtensionSupplier"
    }
}