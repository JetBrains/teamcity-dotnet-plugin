package jetbrains.buildServer.dotnet.test.depcache

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.CommandLineOutputAccumulationObserver
import jetbrains.buildServer.agent.cache.depcache.DependencyCache
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootUsage
import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.depcache.*
import jetbrains.buildServer.depcache.utils.NugetProjectPackagesJsonParser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Path
import kotlin.random.Random

class DotnetDependencyCacheManagerTest {
    @MockK
    private lateinit var _loggerService: LoggerService
    @MockK
    private lateinit var _dotnetDependencyCacheSettingsProvider: DotnetDependencyCacheSettingsProvider
    @MockK
    private lateinit var _buildInfo: BuildInfo
    @MockK
    private lateinit var _cache: DependencyCache
    @MockK
    private lateinit var _invalidator: DotnetPackagesChangedInvalidator

    private lateinit var tempFiles: TempFiles
    private lateinit var cachesDir: File
    private lateinit var stepId: String
    private val projectPackages = """
            {
              "version": 1,
              "parameters": "--include-transitive",
              "projects": [
                {
                  "path": "/a85573f93844cb36/Project1/Prj1.csproj",
                  "frameworks": [
                    {
                      "framework": "net8.0",
                      "topLevelPackages": [
                        {
                          "id": "Serilog.Sinks.Console",
                          "requestedVersion": "6.0.0",
                          "resolvedVersion": "6.0.0"
                        }
                      ],
                      "transitivePackages": [
                        {
                          "id": "Serilog",
                          "resolvedVersion": "4.0.0"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        tempFiles = TempFiles()
        cachesDir = File(tempFiles.createTempDir(), "packages")
        stepId = "dotnet_${Random.nextInt()}"

        every { _dotnetDependencyCacheSettingsProvider.cache } returns _cache
        every { _dotnetDependencyCacheSettingsProvider.postBuildInvalidator } returns _invalidator
        every { _buildInfo.id } returns stepId
    }

    @Test
    fun `should register and restore cache`() {
        // arrange
        val cacheRootUsage = cacheRootUsage()
        val context = mockk<DependencyCacheDotnetStepContext>(relaxed = true)
        val nugetPackagesGlobalDirObserver = mockk<CommandLineOutputAccumulationObserver>()
        every { nugetPackagesGlobalDirObserver.output } returns "global-packages: ${cachesDir.absolutePath}"
        every { context.newCacheRootUsage(any(), any()) } returns cacheRootUsage
        val manager = create()

        // act
        manager.registerAndRestoreCache(context, nugetPackagesGlobalDirObserver)

        // assert
        verify(exactly = 1) { context.newCacheRootUsage(cachesDir.toPath(), stepId) }
        verify(exactly = 1) { _cache.registerAndRestore(cacheRootUsage) }
        verify(exactly = 1) { context.nugetPackagesLocation = cachesDir.toPath() }
    }

    @Test
    fun `should not register and restore cache when cache is disabled`() {
        // arrange
        val context = mockk<DependencyCacheDotnetStepContext>()
        val nugetPackagesGlobalDirObserver = mockk<CommandLineOutputAccumulationObserver>()
        val manager = create()
        every { _dotnetDependencyCacheSettingsProvider.cache } returns null

        // act
        manager.registerAndRestoreCache(context, nugetPackagesGlobalDirObserver)

        // assert
        verify(exactly = 0) { context.newCacheRootUsage(any(), any()) }
        verify(exactly = 0) { _cache.registerAndRestore(any()) }
        verify(exactly = 0) { context.nugetPackagesLocation = any() }
    }

    @Test
    fun `should update invalidation data`() {
        // arrange
        val context = mockk<DependencyCacheDotnetStepContext>(relaxed = true)
        val nugetPackagesGlobalDirObserver = mockk<CommandLineOutputAccumulationObserver>()
        every { nugetPackagesGlobalDirObserver.output } returns projectPackages
        every { context.nugetPackagesLocation } returns cachesDir.toPath()
        val pathSlot = slot<Path>()
        val packagesSlot = slot<DotnetListPackagesResult>()
        val manager = create()

        // act
        manager.updateInvalidationData(context, nugetPackagesGlobalDirObserver)

        // assert
        verify(exactly = 1) { _invalidator.addPackagesToCachesLocation(capture(pathSlot), capture(packagesSlot)) }
        Assert.assertEquals(pathSlot.captured, cachesDir.toPath())
        Assert.assertEquals(packagesSlot.captured, packages())
    }

    @Test
    fun `should not update invalidation data when cache is disabled`() {
        // arrange
        val context = mockk<DependencyCacheDotnetStepContext>()
        val nugetPackagesGlobalDirObserver = mockk<CommandLineOutputAccumulationObserver>()
        val manager = create()
        every { _dotnetDependencyCacheSettingsProvider.cache } returns null

        // act
        manager.updateInvalidationData(context, nugetPackagesGlobalDirObserver)

        // assert
        verify(exactly = 0) { _invalidator.addPackagesToCachesLocation(any(), any()) }
    }

    private fun create() = DotnetDependencyCacheManager(_loggerService, _dotnetDependencyCacheSettingsProvider, _buildInfo)

    private fun cacheRootUsage() = CacheRootUsage(
        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
        cachesDir.toPath(),
        stepId
    )

    private fun packages(): DotnetListPackagesResult = NugetProjectPackagesJsonParser.fromCommandLineOutput(projectPackages).getOrThrow()
}