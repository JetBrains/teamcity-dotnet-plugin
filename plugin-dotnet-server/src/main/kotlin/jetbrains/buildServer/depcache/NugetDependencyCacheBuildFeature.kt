package jetbrains.buildServer.depcache

import jetbrains.buildServer.depcache.DotnetDependencyCacheConstants.FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE
import jetbrains.buildServer.depcache.DotnetDependencyCacheConstants.FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE_DEFAULT
import jetbrains.buildServer.dotnet.DotnetConstants.RUNNER_TYPE
import jetbrains.buildServer.server.cache.depcache.buildFeature.RunnerDependencyCacheBuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class NugetDependencyCacheBuildFeature (
    private val pluginDescriptor: PluginDescriptor,
) : RunnerDependencyCacheBuildFeature(RUNNER_TYPE, FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE, FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE_DEFAULT) {

    public override fun getDisplayName(): String {
        return "NuGet Cache"
    }

    public override fun describeParameters(params: Map<String?, String?>): String {
        return "Caches NuGet packages on .NET steps to speed up the builds"
    }

    public override fun getEditParametersUrl(): String {
        return pluginDescriptor.getPluginResourcesPath("dotnet/editNugetCacheBuildFeature.jsp")
    }
}