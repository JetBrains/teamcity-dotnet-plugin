package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.dotnet.DotnetRunnerCacheDirectoryProvider
import jetbrains.buildServer.dotnet.RestorePackagesPathManager
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetRunnerCacheDirectoryProviderTest {
    @MockK
    private lateinit var agentConfiguration: BuildAgentConfiguration
    private lateinit var tempFiles: TempFiles
    private lateinit var cachesDir: File

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        tempFiles = TempFiles()
        cachesDir = File(tempFiles.createTempDir(), DotnetRunnerCacheDirectoryProvider.DOTNET_CACHE_DIR)

        every { agentConfiguration.getCacheDirectory(any()) } returns cachesDir
    }

    @Test
    fun `should return dotnet runner cache location`() {
        // arrange
        val dotnetRunnerCacheDirectoryProvider = create()

        // act
        val result = dotnetRunnerCacheDirectoryProvider.getDotnetRunnerCacheDirectory(agentConfiguration)

        // assert
        Assert.assertEquals(result, cachesDir)
    }

    private fun create() = DotnetRunnerCacheDirectoryProvider()
}