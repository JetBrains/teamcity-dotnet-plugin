package jetbrains.buildServer.agent

import jetbrains.buildServer.dotnet.DotnetAgentPropertiesProvider
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import org.apache.log4j.Logger

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
                            val prevValue = configuration.configurationParameters.get(property.name)
                            if (prevValue != null) {
                                LOG.warn("Update ${property.name}=\"${property.value}\". Previous value was \"$prevValue\".")
                            } else {
                                LOG.info("Add ${property.name}=\"${property.value}\".")
                            }

                            configuration.addConfigurationParameter(property.name, property.value)
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