package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe

class PropertiesExtension(
        private val _agentPropertiesProviders: List<AgentPropertiesProvider>)
    : EventObserver {
    override fun subscribe(sources: EventSources): Disposable =
            sources.beforeAgentConfigurationLoadedSource.subscribe { event ->
                val configuration = event.agent.configuration
                for (agentPropertiesProvider in _agentPropertiesProviders) {
                    LOG.info("Fetching the agent properties for ${agentPropertiesProvider.desription}")
                    try {
                        for (property in agentPropertiesProvider.properties) {
                            var name = property.name

                            if (property.toolType != ToolInstanceType.DotNetCLI && property.toolType != ToolInstanceType.DotNetSDK) {
                                name = name + "_Test_" + property.toolType.name + "_from_" + agentPropertiesProvider.desription.replace(' ', '_')
                            }

                            val prevValue = configuration.configurationParameters.get(name)
                            if (prevValue != null) {
                                LOG.warn("Update ${name}=\"${property.value}\". Previous value was \"$prevValue\".")
                            } else {
                                LOG.info("Add ${name}=\"${property.value}\".")
                            }

                            configuration.addConfigurationParameter(name, property.value)
                        }
                    }
                    catch (e: Exception) {
                        LOG.debug("Error while fetching the agent properties for ${agentPropertiesProvider.desription}", e)
                    }
                }
            }

    companion object {
        private val LOG = Logger.getLogger(PropertiesExtension::class.java)
    }
}