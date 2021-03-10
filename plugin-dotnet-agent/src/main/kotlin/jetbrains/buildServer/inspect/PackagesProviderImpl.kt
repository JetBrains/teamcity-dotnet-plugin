package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.Logger

class PackagesProviderImpl(
        sources: List<PluginSource>)
    : PackagesProvider {

    private val _sources: Map<String, PluginSource> = sources.associate { it.id.toLowerCase() to it }

    override fun getPackages(specifications: String) =
        E("Packages",
                specifications
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .mapNotNull { specification ->
                            val match = PluginSourceRegex.matchEntire(specification)
                            if (match != null) {
                                val sourceId = match.groupValues[1]
                                val source = _sources[sourceId.toLowerCase()]
                                if (source != null) {
                                    source.getPlugin(match.groupValues[2])
                                } else {
                                    LOG.info("Unrecognized plugin source: $sourceId")
                                    null
                                }
                            } else {
                                LOG.info("Invalid R# plugin specification: $specification")
                                null
                            }
                        }
                        .asSequence()
        )

    companion object {
        private val LOG = Logger.getLogger(PackagesProviderImpl::class.java)
        private val PluginSourceRegex = Regex("([^\\s]+)\\s+(.*)")
    }
}
