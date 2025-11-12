package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty

class MSBuildAgentPropertiesProvider(
    private val _msBuildFileSystemProvider: MSBuildFileSystemAgentPropertiesProvider,
    private val _msBuildRegistryProvider: MSBuildRegistryAgentPropertiesProvider,
) : AgentPropertiesProvider {

    override val description = "MSBuild"

    override val properties: Sequence<AgentProperty>
        get() {
            val properties1 = _msBuildFileSystemProvider.properties
            val properties2 = _msBuildRegistryProvider.properties
            return (properties1 + properties2)
                .groupBy { Pair(it.toolType, it.name) }
                .values
                .map { properties -> deduplicate(properties) }
                .asSequence()
        }

    private fun deduplicate(properties: List<AgentProperty>): AgentProperty {
        if (properties.size == 1) {
            return properties.first()
        }
        // A better solution would probably include checking the "product.id" field from the
        // "C:\ProgramData\Microsoft\VisualStudio\Packages\_Instances\*\state.json".
        // Examples of product IDs:
        // - "Microsoft.VisualStudio.Product.BuildTools"
        // - "Microsoft.VisualStudio.Product.Professional"
        // - "Microsoft.VisualStudio.Product.Ssms"
        //
        // But checking the "state.json" is not enough, because MSBuild can be installed
        // separately from VisualStudio. Or it can be detected in Windows Registry.
        //
        // Given these complications, the logic below looks like a good workaround.
        // It adds at least some determinism in case of duplicates and fixes TW-93643 and TW-60248
        val msBuildFromBuildTools = properties.find { it.value.contains("BuildTools") }
        if (msBuildFromBuildTools != null) {
            return msBuildFromBuildTools
        }
        val msBuildNotFromSqlStudio = properties.find { !it.value.contains("SQL") }
        return msBuildNotFromSqlStudio ?: properties.first()
    }
}