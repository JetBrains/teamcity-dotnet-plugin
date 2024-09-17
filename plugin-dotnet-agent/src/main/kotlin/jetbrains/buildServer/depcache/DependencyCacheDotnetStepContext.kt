package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootUsage
import java.nio.file.Path

class DependencyCacheDotnetStepContext {

    private var executionNumber = 0
    var nugetPackagesLocation: Path? = null

    fun newCacheRootUsage(nugetPackagesPath: Path, stepId: String): CacheRootUsage {
        return CacheRootUsage(
            DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
            nugetPackagesPath.toAbsolutePath(),
            "$stepId.${executionNumber++}"
        )
    }

    companion object {
        fun newContext(): DependencyCacheDotnetStepContext = DependencyCacheDotnetStepContext()
    }
}