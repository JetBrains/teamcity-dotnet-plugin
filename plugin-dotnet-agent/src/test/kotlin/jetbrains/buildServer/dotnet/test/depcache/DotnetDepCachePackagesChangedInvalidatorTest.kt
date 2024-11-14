package jetbrains.buildServer.dotnet.test.depcache

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRoot
import jetbrains.buildServer.agent.cache.depcache.cacheroot.CacheRootDescriptor
import jetbrains.buildServer.agent.cache.depcache.invalidation.Deserializer
import jetbrains.buildServer.agent.cache.depcache.invalidation.InvalidationMetadata
import jetbrains.buildServer.agent.cache.depcache.invalidation.InvalidationResult
import jetbrains.buildServer.agent.cache.depcache.invalidation.Serializable
import jetbrains.buildServer.depcache.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.Paths

class DotnetDepCachePackagesChangedInvalidatorTest {

    private lateinit var instance: DotnetDepCachePackagesChangedInvalidator
    @MockK private lateinit var invalidationMetadataMock: InvalidationMetadata

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        instance = DotnetDepCachePackagesChangedInvalidator()
    }

    @DataProvider
    fun getPackagesNotChangedTestData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                """
                    {
                      "cacheRootPackages": {
                        "cache-root-1": [
                          "Serilog.Sinks.Console:6.0.0",
                          "FluentValidation:11.9.2",
                          "Humanizer:2.14.1"
                        ],
                        "cache-root-2": [
                          "MediatR:12.4.0",
                          "MediatR.Contracts:2.0.1"
                        ]
                      }
                    }
                """.trimIndent(),
                listOf(
                    CacheRoot(
                        "cache-root-1",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages1"),
                        emptySet<String>()
                    ),
                    CacheRoot(
                        "cache-root-2",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages2"),
                        emptySet<String>()
                    )
                ),
                mapOf(
                    Paths.get("/nuget/.packages1") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Serilog.Sinks.Console", resolvedVersion = "6.0.0"),
                                            Package(id = "FluentValidation", resolvedVersion = "11.9.2")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            ),
                            Project(
                                path = "/Module2.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Humanizer", resolvedVersion = "2.14.1")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Paths.get("/nuget/.packages2") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "MediatR", resolvedVersion = "12.4.0")
                                        ),
                                        transitivePackages = listOf(
                                            Package(id = "MediatR.Contracts", resolvedVersion = "2.0.1")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "getPackagesNotChangedTestData")
    fun `should not invalidate cache when package sets not changed`(
        cachedPackagesJson: String,
        newCacheRoots: List<CacheRoot>,
        repoPathToPackages: Map<Path, DotnetDepCacheListPackagesResult>
    ) {
        // arrange
        val parameterName = "nugetPackages"
        val cachedPackages: DotnetDepCacheNugetPackages = prepareNugetPackagesMetadata(cachedPackagesJson)
        every { invalidationMetadataMock.getObjectParameter(any(), any<Deserializer<Serializable>>()) } returns cachedPackages
        val serializableArgumentSlot = slot<Serializable>()

        // act
        repoPathToPackages.forEach {
            instance.addPackagesToCachesLocation(it.key, it.value)
        }
        val invalidationResult: InvalidationResult = instance.run(invalidationMetadataMock, emptyList<CacheRootDescriptor>(), newCacheRoots)

        // assert
        Assert.assertFalse(invalidationResult.isInvalidated)
        Assert.assertNull(invalidationResult.invalidationReason)
        verify { invalidationMetadataMock.getObjectParameter(parameterName, any<Deserializer<Serializable>>()) }
        verify { invalidationMetadataMock.publishObjectParameter(parameterName, capture(serializableArgumentSlot)) }
        val capturedPackagesToPublish: DotnetDepCacheNugetPackages = serializableArgumentSlot.captured as DotnetDepCacheNugetPackages
        Assert.assertEquals(capturedPackagesToPublish, cachedPackages) // we publish the same package sets
    }

    @DataProvider
    fun getPackagesChangedTestData(): Array<Array<Any>> {
        return arrayOf<Array<Any>>(
            // set 1: a package version changed
            arrayOf(
                """
                    {
                      "cacheRootPackages": {
                        "cache-root-1": [
                          "Serilog.Sinks.Console:6.0.0", // <-- old version
                          "FluentValidation:11.9.2",
                          "Humanizer:2.14.1"
                        ],
                        "cache-root-2": [
                          "MediatR:12.4.0",
                          "MediatR.Contracts:2.0.1"
                        ]
                      }
                    }
                """.trimIndent(),
                listOf(
                    CacheRoot(
                        "cache-root-1",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages1"),
                        emptySet<String>()
                    ),
                    CacheRoot(
                        "cache-root-2",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages2"),
                        emptySet<String>()
                    )
                ),
                mapOf(
                    Paths.get("/nuget/.packages1") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Serilog.Sinks.Console", resolvedVersion = "6.1.0"), // <-- new version
                                            Package(id = "FluentValidation", resolvedVersion = "11.9.2"),
                                            Package(id = "Humanizer", resolvedVersion = "2.14.1")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Paths.get("/nuget/.packages2") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "MediatR", resolvedVersion = "12.4.0")
                                        ),
                                        transitivePackages = listOf(
                                            Package(id = "MediatR.Contracts", resolvedVersion = "2.0.1")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),

//            // set 2: a new package added
            arrayOf(
                """
                    {
                      "cacheRootPackages": {
                        "cache-root-1": [
                          "Serilog.Sinks.Console:6.0.0",
                          "FluentValidation:11.9.2",
                          "Humanizer:2.14.1"
                        ],
                        "cache-root-2": [
                          "MediatR:12.4.0",
                          "MediatR.Contracts:2.0.1"
                        ]
                      }
                    }
                """.trimIndent(),
                listOf(
                    CacheRoot(
                        "cache-root-1",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages1"),
                        emptySet<String>()
                    ),
                    CacheRoot(
                        "cache-root-2",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages2"),
                        emptySet<String>()
                    )
                ),
                mapOf(
                    Paths.get("/nuget/.packages1") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Serilog.Sinks.Console", resolvedVersion = "6.0.0"),
                                            Package(id = "FluentValidation", resolvedVersion = "11.9.2"),
                                            Package(id = "Humanizer", resolvedVersion = "2.14.1")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Paths.get("/nuget/.packages2") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "MediatR", resolvedVersion = "12.4.0")
                                        ),
                                        transitivePackages = listOf(
                                            Package(id = "MediatR.Contracts", resolvedVersion = "2.0.1"),
                                            Package(id = "MediatR.NewDep", resolvedVersion = "2.0.1") // <-- new package
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),

//            // set 3: a package deleted
            arrayOf(
                """
                    {
                      "cacheRootPackages": {
                        "cache-root-1": [
                          "Serilog.Sinks.Console:6.0.0",
                          "FluentValidation:11.9.2",
                          "Humanizer:2.14.1" // <-- deleted package
                        ],
                        "cache-root-2": [
                          "MediatR:12.4.0",
                          "MediatR.Contracts:2.0.1"
                        ]
                      }
                    }
                """.trimIndent(),
                listOf(
                    CacheRoot(
                        "cache-root-1",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages1"),
                        emptySet<String>()
                    ),
                    CacheRoot(
                        "cache-root-2",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages2"),
                        emptySet<String>()
                    )
                ),
                mapOf(
                    Paths.get("/nuget/.packages1") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Serilog.Sinks.Console", resolvedVersion = "6.0.0"),
                                            Package(id = "FluentValidation", resolvedVersion = "11.9.2")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Paths.get("/nuget/.packages2") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "MediatR", resolvedVersion = "12.4.0")
                                        ),
                                        transitivePackages = listOf(
                                            Package(id = "MediatR.Contracts", resolvedVersion = "2.0.1")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),

//            // set 4: a package moved from one cache root to another
            arrayOf(
                """
                    {
                      "cacheRootPackages": {
                        "cache-root-1": [
                          "Serilog.Sinks.Console:6.0.0",
                          "FluentValidation:11.9.2",
                          "Humanizer:2.14.1" // <-- it was here
                        ],
                        "cache-root-2": [
                          "MediatR:12.4.0",
                          "MediatR.Contracts:2.0.1"
                        ]
                      }
                    }
                """.trimIndent(),
                listOf(
                    CacheRoot(
                        "cache-root-1",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages1"),
                        emptySet<String>()
                    ),
                    CacheRoot(
                        "cache-root-2",
                        DotnetDependencyCacheConstants.CACHE_ROOT_TYPE,
                        Paths.get("/nuget/.packages2"),
                        emptySet<String>()
                    )
                ),
                mapOf(
                    Paths.get("/nuget/.packages1") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "Serilog.Sinks.Console", resolvedVersion = "6.0.0"),
                                            Package(id = "FluentValidation", resolvedVersion = "11.9.2")
                                        ),
                                        transitivePackages = emptyList()
                                    )
                                )
                            )
                        )
                    ),
                    Paths.get("/nuget/.packages2") to DotnetDepCacheListPackagesResult(
                        version = 1,
                        parameters = "--include-transitive",
                        problems = emptyList(),
                        projects = listOf(
                            Project(
                                path = "/Module1.csproj",
                                frameworks = listOf(
                                    Framework(
                                        framework = "net8.0",
                                        topLevelPackages = listOf(
                                            Package(id = "MediatR", resolvedVersion = "12.4.0"),
                                            Package(id = "Humanizer", resolvedVersion = "2.14.1") // <-- now it's here
                                        ),
                                        transitivePackages = listOf(
                                            Package(id = "MediatR.Contracts", resolvedVersion = "2.0.1")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
        )
    }

    @Test(dataProvider = "getPackagesChangedTestData")
    fun `should invalidate cache when package sets changed`(
        cachedPackagesJson: String,
        newCacheRoots: List<CacheRoot>,
        repoPathToPakages: Map<Path, DotnetDepCacheListPackagesResult>
    ) {
        // arrange
        val parameterName = "nugetPackages"
        var cachedPackages: DotnetDepCacheNugetPackages = prepareNugetPackagesMetadata(cachedPackagesJson)
        every { invalidationMetadataMock.getObjectParameter(any(), any<Deserializer<Serializable>>()) } returns cachedPackages
        val serializableArgumentSlot = slot<Serializable>()

        // act
        repoPathToPakages.forEach {
            instance.addPackagesToCachesLocation(it.key, it.value)
        }
        var invalidationResult: InvalidationResult = instance.run(invalidationMetadataMock, emptyList<CacheRootDescriptor>(), newCacheRoots)

        // assert
        Assert.assertTrue(invalidationResult.isInvalidated)
        Assert.assertNotNull(invalidationResult.invalidationReason)
        verify { invalidationMetadataMock.getObjectParameter(parameterName, any<Deserializer<Serializable>>()) }
        verify { invalidationMetadataMock.publishObjectParameter(parameterName, capture(serializableArgumentSlot)) }
        var capturedPackagesToPublish: DotnetDepCacheNugetPackages? = serializableArgumentSlot.captured as DotnetDepCacheNugetPackages?
        Assert.assertNotEquals(capturedPackagesToPublish, cachedPackages) // we publish changed package sets
    }


    private fun prepareNugetPackagesMetadata(json: String): DotnetDepCacheNugetPackages {
        return DotnetDepCacheNugetPackages.deserialize(json.toByteArray(StandardCharsets.UTF_8))
    }
}