package jetbrains.buildServer.depcache

object DotnetDependencyCacheConstants {
    const val CACHE_ROOT_TYPE: String = "nuget-packages"
    const val CACHE_DISPLAY_NAME: String = "NuGet"
    const val FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE: String = "teamcity.internal.depcache.buildFeature.dotnet.enabled"
    const val FEATURE_TOGGLE_DOTNET_DEPENDENCY_CACHE_DEFAULT: Boolean = false
}