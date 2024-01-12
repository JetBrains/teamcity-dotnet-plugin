

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