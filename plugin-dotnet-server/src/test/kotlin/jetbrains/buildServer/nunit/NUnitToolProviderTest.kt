package jetbrains.buildServer.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.HttpDownloader
import jetbrains.buildServer.tools.available.AvailableToolsFetcher
import jetbrains.buildServer.tools.available.DownloadableToolVersion
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult
import jetbrains.buildServer.util.TimeService
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class NUnitToolProviderTest {
    @MockK
    private lateinit var _timeService: TimeService

    @MockK
    private lateinit var _availableToolsFetcher: AvailableToolsFetcher

    @MockK
    private lateinit var _httpDownloader: HttpDownloader

    @MockK
    private lateinit var _fileSystem: FileSystemService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should get available tool versions`() {
        // arrange
        val tool1 = mockk<DownloadableToolVersion>(name = "DownloadableToolVersion1") {
            every { version } returns "1"
        }
        val tool2 = mockk<DownloadableToolVersion>(name = "DownloadableToolVersion2") {
            every { version } returns "2"
        }
        every { _availableToolsFetcher.fetchAvailable() } returns FetchAvailableToolsResult
            .createSuccessful(listOf(tool2, tool1))

        every { _timeService.now() } returns 10

        val provider = createProvider()

        // act
        val tools = provider.availableToolVersions

        // assert
        assertEquals(tools.size, 2)
        assertTrue(tools.contains(tool1))
        assertTrue(tools.contains(tool2))
    }

    @Test
    fun `should fetch tool package`() {
        // arrange
        val tool = mockk<DownloadableToolVersion>(name = "DownloadableToolVersion1") {
            every { version } returns "1"
            every { displayName } returns "tool-name"
            every { downloadUrl } returns "http://github.com"
            every { destinationFileName } returns "pack.zip"
        }
        every { _availableToolsFetcher.fetchAvailable() } returns FetchAvailableToolsResult
            .createSuccessful(listOf(tool))

        every { _timeService.now() } returns 10

        justRun { _fileSystem.write(any(), any()) }

        val provider = createProvider()

        // act
        val fetchedFile = provider.fetchToolPackage(tool, File("targetDir"))

        // assert
        assertEquals(fetchedFile, File("targetDir", "pack.zip"))
    }

    private fun createProvider() = NUnitToolProvider(
        _timeService,
        _availableToolsFetcher,
        _httpDownloader,
        _fileSystem
    )
}