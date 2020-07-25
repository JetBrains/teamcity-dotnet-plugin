package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.AgentPropertyType
import org.apache.log4j.Logger

class VisualStudioAgentPropertiesProvider(
        private val _visualStudioProviders: List<VisualStudioProvider>)
    : AgentPropertiesProvider {

    override val desription = "Visual Studio"

    override val properties =
            _visualStudioProviders
                    .asSequence()
                    .flatMap { it.getInstances() }
                    .distinctBy { it.productLineVersion }
                    .flatMap {
                        visualStudio ->
                        LOG.info("Found ${visualStudio}.")
                        sequence {
                            yield(AgentProperty(AgentPropertyType.VisualStudio, "VS${visualStudio.productLineVersion}", "${visualStudio.displayVersion}"))
                            yield(AgentProperty(AgentPropertyType.VisualStudio, "VS${visualStudio.productLineVersion}_Path", visualStudio.installationPath.path))
                        }
                    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioAgentPropertiesProvider::class.java)
    }
}