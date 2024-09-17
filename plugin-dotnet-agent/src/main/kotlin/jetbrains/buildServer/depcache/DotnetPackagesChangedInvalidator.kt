package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRoot
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootDescriptor
import jetbrains.buildServer.agent.cache.depcache.invalidation.DependencyCacheInvalidator
import jetbrains.buildServer.agent.cache.depcache.invalidation.InvalidationMetadata
import jetbrains.buildServer.agent.cache.depcache.invalidation.InvalidationResult
import java.nio.file.Path

class DotnetPackagesChangedInvalidator : DependencyCacheInvalidator {

    private val absolutePackagesPathToProjectPathToPackages: MutableMap<String, MutableMap<String, MutableList<Framework>>> = HashMap()

    override fun run(
        invalidationMetadata: InvalidationMetadata,
        cacheRoots: List<CacheRootDescriptor>,
        newCacheRoots: List<CacheRoot>
    ): InvalidationResult {
        val absoluteCachesPathToNewCacheRootId: Map<String, String> = newCacheRoots.associate {
            it.location.toAbsolutePath().toString() to it.id
        }

        val newCacheRootIdToPackagesSet: Map<String, Set<String>> = absolutePackagesPathToProjectPathToPackages.entries.mapNotNull { entry ->
            absoluteCachesPathToNewCacheRootId[entry.key]?.let { cacheRootId ->
                cacheRootId to extractPackages(entry.value)
            }
        }.toMap()

        val newPackages = NugetPackages(newCacheRootIdToPackagesSet)
        val cachedPackages = invalidationMetadata.getObjectParameter("nugetPackages") {
            NugetPackages.deserialize(it)
        }

        invalidationMetadata.publishObjectParameter<NugetPackages>("nugetPackages", newPackages)

        return if (newPackages == cachedPackages) InvalidationResult.validated()
        else InvalidationResult.invalidated("Nuget packages have changed")
    }

    private fun extractPackages(projectPathToPackages: MutableMap<String, MutableList<Framework>>): Set<String> {
        return projectPathToPackages.values
            .flatMap { it }
            .flatMap { framework ->
                (framework.topLevelPackages ?: emptyList()) + (framework.transitivePackages ?: emptyList())
            }
            .mapNotNull { it.packageCompositeName }
            .toSet()
    }

    override fun shouldRunIfCacheInvalidated(): Boolean = true

    fun addPackagesToCachesLocation(nugetPackagesPath: Path, nugetPackages: DotnetListPackagesResult) {
        nugetPackages.projects?.forEach { project ->
            if (project.path != null && !project.frameworks.isNullOrEmpty()) {
                absolutePackagesPathToProjectPathToPackages.getOrPut(nugetPackagesPath.toAbsolutePath().toString()) {
                    HashMap()
                }.getOrPut(project.path) {
                    ArrayList()
                }.addAll(project.frameworks.toSet())
            }
        }
    }
}