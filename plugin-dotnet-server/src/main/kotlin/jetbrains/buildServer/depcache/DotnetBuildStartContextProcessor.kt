package jetbrains.buildServer.depcache

import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.TeamCityProperties

class DotnetBuildStartContextProcessor : BuildStartContextProcessor {

    override fun updateParameters(context: BuildStartContext) {
        mapDotnetDependencyCacheEnabledInternalProperty(context)
    }

    /**
     * Allows enabling/disabling agent-side of the .NET dependency cache via an internal server property.
     */
    private fun mapDotnetDependencyCacheEnabledInternalProperty(context: BuildStartContext) {
        val propertyValue = TeamCityProperties.getPropertyOrNull(DotnetDependencyCacheConstants.DEP_CACHE_ENABLED) ?: return
        val buildType = context.build.buildType ?: return

        if (!buildType.configParameters.containsKey(DotnetDependencyCacheConstants.DEP_CACHE_ENABLED)) {
            context.addSharedParameter(DotnetDependencyCacheConstants.DEP_CACHE_ENABLED, propertyValue)
        }
    }
}