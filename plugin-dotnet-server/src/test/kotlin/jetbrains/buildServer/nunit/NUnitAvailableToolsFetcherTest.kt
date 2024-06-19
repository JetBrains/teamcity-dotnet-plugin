package jetbrains.buildServer.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.HttpDownloader
import jetbrains.buildServer.tools.available.DownloadableToolVersion
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NUnitAvailableToolsFetcherTest {
    @MockK
    private lateinit var _httpDownloader: HttpDownloader

    @MockK
    private lateinit var _releasesParser: ToolsParser

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        justRun { _httpDownloader.download(any(), any()) }
    }

    @Test
    fun `should fetch available nunit tools`() {
        // arrange
        val tool1 = mockk<DownloadableToolVersion>(name = "DownloadableToolVersion1") {
            every { version } returns "1"
        }

        val tool2 = mockk<DownloadableToolVersion>(name = "DownloadableToolVersion2") {
            every { version } returns "2"
        }

        every { _releasesParser.parse(any()) } returns mutableListOf(tool1, tool2)

        // act
        val fetcher = NUnitAvailableToolsFetcher(_httpDownloader, _releasesParser)
        val result = fetcher.fetchAvailable()

        // assert
        assertEquals(result.fetchedTools.size, 2)
        assertTrue(result.fetchedTools.contains(tool1))
        assertTrue(result.fetchedTools.contains(tool2))
    }
}