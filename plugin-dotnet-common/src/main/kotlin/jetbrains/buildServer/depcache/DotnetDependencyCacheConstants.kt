package jetbrains.buildServer.depcache

object DotnetDependencyCacheConstants {
    const val CACHE_ROOT_TYPE: String = "nuget-packages"
    const val DEP_CACHE_ENABLED: String = "teamcity.internal.depcache.buildFeature.dotnet.enabled"
    const val DEP_CACHE_ENABLED_DEFAULT: Boolean = false
}