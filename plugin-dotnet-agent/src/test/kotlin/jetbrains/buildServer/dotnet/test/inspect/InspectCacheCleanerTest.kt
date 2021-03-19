package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.inspect.InspectCacheCleaner
import jetbrains.buildServer.inspect.InspectCodeConstants
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class InspectCacheCleanerTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideTargets() {
        // Given
        val cleaner = createInstance()
        val cache = File("Cache")
        every { _pathService.getPath(PathType.CachePerCheckout, InspectCodeConstants.RUNNER_TYPE) } returns cache

        // When
        var actualTargets = cleaner.targets.toList()

        // Then
        Assert.assertEquals(actualTargets, listOf(cache))
    }

    @Test
    fun shouldClean() {
        // Given
        val cleaner = createInstance()
        val cache = File("Cache")
        every { _fileSystemService.remove(any()) } returns true

        // When
        val actualResult = cleaner.clean(cache)

        // Then
        Assert.assertEquals(actualResult, true)
        verify { _fileSystemService.remove(cache) }
    }

    private fun createInstance() =
            InspectCacheCleaner(
                    "name",
                    CleanType.Light,
                    InspectCodeConstants.RUNNER_TYPE,
                    PathType.CachePerCheckout,
                    _pathService,
                    _fileSystemService)
}