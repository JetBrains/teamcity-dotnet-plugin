package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheProvider
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheSettingsProviderRegistry
import jetbrains.buildServer.agent.cache.depcache.buildFeature.BuildRunnerDependencyCacheSettingsProvider
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootPublishPaths
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootPublisher
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher

class DotnetDependencyCacheSettingsProvider(
    private val eventDispatcher: EventDispatcher<AgentLifeCycleListener>,
    private val cacheSettingsProviderRegistry: DependencyCacheSettingsProviderRegistry,
    private val cacheProvider: DependencyCacheProvider
) : BuildRunnerDependencyCacheSettingsProvider<DotnetPackagesChangedInvalidator>(
    eventDispatcher, cacheSettingsProviderRegistry, cacheProvider,
    DotnetConstants.RUNNER_TYPE,
    DotnetDependencyCacheConstants.DEP_CACHE_ENABLED,
    DotnetDependencyCacheConstants.DEP_CACHE_ENABLED_DEFAULT) {

    protected override fun createPostBuildInvalidator(): DotnetPackagesChangedInvalidator {
        return DotnetPackagesChangedInvalidator()
    }

    protected override fun createCacheRootPublisher(): CacheRootPublisher {
        return CacheRootPublisher({
            CacheRootPublishPaths.includeAll()
        })
    }
}