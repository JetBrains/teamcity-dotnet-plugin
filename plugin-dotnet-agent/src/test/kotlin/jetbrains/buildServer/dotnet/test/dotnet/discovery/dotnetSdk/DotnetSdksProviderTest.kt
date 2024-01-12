

package jetbrains.buildServer.dotnet.test.dotnet.discovery.dotnetSdk

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProviderImpl
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetSdksProviderTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3"))
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3-rc"))
                                .addFile(File(File("dotnet", "sdk"), "1.2.4"))
                                .addDirectory(File(File("dotnet", "sdk"), "nuget"))
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.5")),
                        listOf(
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.3"), Version(1, 2, 3)),
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.3-rc"), Version.parse("1.2.3-rc")),
                                DotnetSdk(File(File("dotnet", "sdk"), "1.2.5"), Version(1, 2, 5))
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File(File("dotnet", "sdk"), "1.2.3"))
                                .addFile(File(File("dotnet", "sdk"), "1.2.4")),
                        listOf(DotnetSdk(File(File("dotnet", "sdk"), "1.2.3"), Version(1, 2, 3)))
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("dotnet", "sdk")),
                        emptyList<DotnetSdk>()
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideSdks(fileSystemService: FileSystemService, expectedSdks: List<DotnetSdk>) {
        // Given
        val toolPath = File("dotnet", "dotnet.exe")
        val provider = createInstance(fileSystemService)

        // When
        val actualSdks = provider.getSdks(toolPath).toList()

        // Then
        Assert.assertEquals(actualSdks, expectedSdks)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetSdksProviderImpl(fileSystemService)

    companion object {
        private val sdksPath = File(File(File("Program Files"), "dotnet"), "sdk")
    }
}