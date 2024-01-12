

package jetbrains.buildServer.dotnet.test.dotnet.discovery.dotnetRuntime

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntime
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProviderImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetRuntimesProviderTest {
    @MockK
    private lateinit var _toolProvider: ToolProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3-rc"))
                                .addFile(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.4"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "nuget"))
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.5")),
                        listOf(
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"), Version(1, 2, 3), "NETCore"),
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3-rc"), Version.parse("1.2.3-rc"), "NETCore"),
                                DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.5"), Version(1, 2, 5), "NETCore")
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"))
                                .addFile(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.4")),
                        listOf(DotnetRuntime(File(File(File("dotnet", "shared"), "Microsoft.NETCore.App"), "1.2.3"), Version(1, 2, 3), "NETCore"))
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("dotnet", "shared"), "Microsoft.NETCore.App")),
                        emptyList<DotnetSdk>()
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideRuntimes(fileSystemService: FileSystemService, expectedRuntimes: List<DotnetRuntime>) {
        // Given
        val toolPath = File("dotnet", "dotnet.exe")
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns toolPath.path
        val provider = createInstance(fileSystemService)

        // When
        val actualRuntimes = provider.getRuntimes().toList()

        // Then
        Assert.assertEquals(actualRuntimes, expectedRuntimes)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetRuntimesProviderImpl(fileSystemService, _toolProvider)

    companion object {
        private val runtimesPath = File(File(File("Program Files"), "dotnet"), "shared")
    }
}