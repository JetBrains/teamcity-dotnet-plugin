package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheProvider
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheSettingsProviderRegistry
import jetbrains.buildServer.agent.cache.depcache.buildFeature.RunnerDependencyCacheSettingsProvider
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootPublishPaths
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootPublisher
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher

class DotnetDependencyCacheSettingsProvider(
    private val eventDispatcher: EventDispatcher<AgentLifeCycleListener>,
    private val cacheSettingsProviderRegistry: DependencyCacheSettingsProviderRegistry,
    private val cacheProvider: DependencyCacheProvider
) : RunnerDependencyCacheSettingsProvider<DotnetPackagesChangedInvalidator>(
    eventDispatcher, cacheSettingsProviderRegistry, cacheProvider,
    DotnetConstants.RUNNER_TYPE,
    DotnetConstants.RUNNER_DISPLAY_NAME,
    DotnetDependencyCacheConstants.CACHE_DISPLAY_NAME,
    DotnetDependencyCacheConstants.FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE,
    DotnetDependencyCacheConstants.FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE_DEFAULT
) {

    protected override fun createPostBuildInvalidator(): DotnetPackagesChangedInvalidator {
        return DotnetPackagesChangedInvalidator()
    }

    protected override fun createCacheRootPublisher(): CacheRootPublisher {
        return CacheRootPublisher({
            CacheRootPublishPaths.includeAll()
        })
    }
}