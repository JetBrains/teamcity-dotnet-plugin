package jetbrains.buildServer.depcache

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.server.cache.depcache.buildFeature.DependencyCacheBuildFeatureRunnersRegistry
import jetbrains.buildServer.server.cache.depcache.buildFeature.DependencyCacheBuildFeatureSupportedRunner

class DotnetDepCacheRegistrator(private val runnersRegistry: DependencyCacheBuildFeatureRunnersRegistry) {
    private val runner = DependencyCacheBuildFeatureSupportedRunner(DotnetConstants.RUNNER_TYPE, DotnetConstants.RUNNER_DISPLAY_NAME)

    fun register() {
        runnersRegistry.register(runner)
    }

    fun unregister() {
        runnersRegistry.unregister(runner)
    }
}