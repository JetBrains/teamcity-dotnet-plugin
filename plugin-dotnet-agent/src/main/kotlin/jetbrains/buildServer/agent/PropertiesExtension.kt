package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class PropertiesExtension(
        private val _dispatcher: CoroutineDispatcher,
        private val _agentPropertiesProviders: List<AgentPropertiesProvider>)
    : EventObserver {
    private val _lockObject = Object()

    override fun subscribe(sources: EventSources): Disposable =
        sources.beforeAgentConfigurationLoadedSource.subscribe { event ->
            val configuration = event.agent.configuration
            LOG.infoBlock("Fetched agent properties").use {
                runBlocking {
                    for (agentPropertiesProvider in _agentPropertiesProviders) {
                        launch(_dispatcher) {
                            fetchProperties(agentPropertiesProvider, configuration)
                        }
                    }
                }
            }
        }

    private suspend fun fetchProperties(agentPropertiesProvider: AgentPropertiesProvider, configuration: BuildAgentConfiguration) {
        LOG.debugBlock("Fetching agent properties for ${agentPropertiesProvider.desription}").use {
            try {
                for (property in agentPropertiesProvider.properties) {
                    var name = property.name
                    synchronized(_lockObject) {
                        val prevValue = configuration.configurationParameters.get(name)
                        if (prevValue != null) {
                            LOG.warn("Update ${name}=\"${property.value}\". Previous value was \"$prevValue\".")
                        } else {
                            LOG.info("${name}=\"${property.value}\".")
                        }

                        configuration.addConfigurationParameter(name, property.value)
                    }
                }
            } catch (e: Exception) {
                LOG.debug("Error while fetching the agent properties for ${agentPropertiesProvider.desription}", e)
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(PropertiesExtension::class.java)
    }
}