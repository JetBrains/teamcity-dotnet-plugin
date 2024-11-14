package jetbrains.buildServer.depcache

import jetbrains.buildServer.agent.CommandLineOutputAccumulationObserver
import jetbrains.buildServer.agent.cache.depcache.DependencyCache
import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.depcache.utils.DotnetDepCacheGlobalPackagesLocationParser
import jetbrains.buildServer.depcache.utils.DotnetDepCacheProjectPackagesJsonParser
import java.io.File

class DotnetDepCacheManager(
    private val _loggerService: LoggerService,
    private val _dotnetDepCacheSettingsProvider: DotnetDepCacheSettingsProvider,
    private val _buildInfo: BuildInfo
) {

    val cache: DependencyCache?
        get() = _dotnetDepCacheSettingsProvider.cache

    val cacheEnabled: Boolean
        get() = cache != null // not null when cache is enabled and configured for dotnet runner

    fun registerAndRestoreCache(
        depCacheContext: DotnetDepCacheStepContext,
        nugetPackagesGlobalDirObserver: CommandLineOutputAccumulationObserver
    ) {
        val packagesRawOutput = nugetPackagesGlobalDirObserver.output
        if (packagesRawOutput.isNullOrEmpty()) {
            cache?.logWarning("Failed to detect global nuget packages location for the current .NET execution, it will not be cached")
            return
        }
        val globalNugetPackagesLocation = DotnetDepCacheGlobalPackagesLocationParser.fromCommandLineOutput(packagesRawOutput)
        if (globalNugetPackagesLocation == null) {
            cache?.logWarning("""
                Failed to parse global nuget packages location from: $packagesRawOutput.
                The current .NET execution will not be cached
            """.trimIndent())
            return
        }

        registerAndRestoreCache(depCacheContext, File(globalNugetPackagesLocation))
    }

    fun registerAndRestoreCache(
        depCacheContext: DotnetDepCacheStepContext,
        nugetPackagesLocation: File
    ) {
        val cache = cache
        if (cache == null) {
            // this is not an expected case, something is wrong
            _loggerService.writeWarning(".NET dependency cache is enabled but failed to initialize, it will not be used at the current execution")
            return
        }

        if (!nugetPackagesLocation.exists()) {
            _loggerService.writeDebug("Nuget packages location doesn't exist, creating it: ${nugetPackagesLocation.absolutePath}")
            nugetPackagesLocation.mkdirs()
        }
        val nugetPackagesPath = nugetPackagesLocation.toPath()

        _loggerService.writeDebug("Creating a new cache root usage for nuget packages location: $nugetPackagesPath")
        val cacheRootUsage = depCacheContext.newCacheRootUsage(nugetPackagesPath, _buildInfo.id)
        cache.registerAndRestore(cacheRootUsage)

        depCacheContext.nugetPackagesLocation = nugetPackagesPath
    }

    /**
     * Must be invoked after [registerAndRestoreCache]
     */
    fun updateInvalidationData(
        depCacheContext: DotnetDepCacheStepContext,
        nugetPackagesGlobalDirObserver: CommandLineOutputAccumulationObserver
    ) {
        val cache = cache
        val invalidator = _dotnetDepCacheSettingsProvider.postBuildInvalidator
        if (cache == null || invalidator == null) {
            // this is not an expected case, something is wrong
            _loggerService.writeWarning(".NET dependency cache is enabled but failed to initialize, it will not be used at the current execution")
            return
        }

        val projectPackagesRawOutput = nugetPackagesGlobalDirObserver.output
        if (projectPackagesRawOutput.isNullOrEmpty()) {
            cache.logWarning("Failed to collect .NET project's packages, this execution will not be cached")
            return
        }

        val projectPackages = DotnetDepCacheProjectPackagesJsonParser.fromCommandLineOutput(projectPackagesRawOutput).fold(
            onSuccess = { it },
            onFailure = { e ->
                cache.logWarning("Failed to parse .NET project's packages, this execution will not be cached: ${e.message}")
                return
            }
        )
        if (!projectPackages.problems.isNullOrEmpty()) {
            val problems = projectPackages.problems.joinToString("\n") { it.text.orEmpty() }
            cache.logWarning("""
                Problems encountered while collecting .NET project's packages:
                $problems
                This execution will not be cached.
            """.trimIndent())
            return
        }
        if (depCacheContext.nugetPackagesLocation == null) {
            cache.logWarning("NuGet packages location hasn't been initialized, this execution will not be cached")
            return
        }

        invalidator.addPackagesToCachesLocation(depCacheContext.nugetPackagesLocation!!, projectPackages)
    }
}