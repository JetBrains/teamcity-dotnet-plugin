package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetSdksProviderTest {
    @Test
    fun shouldGetDotnetInfoWhenSdksListCommandIsNotSupported() {
        // Given
        val toolPath = File("dotnet", "dotnet.exe")
        val fileSystemService = VirtualFileSystemService()
                .addDirectory(File(File("dotnet", "sdk"), "1.2.3"))
                .addDirectory(File(File("dotnet", "sdk"), "1.2.3-rc"))
                .addFile(File(File("dotnet", "sdk"), "1.2.4"))
                .addDirectory(File(File("dotnet", "sdk"), "nuget"))
                .addDirectory(File(File("dotnet", "sdk"), "1.2.5"))
        val dotnetCliToolInfo = createInstance(fileSystemService)

        // When
        val actualSdks = dotnetCliToolInfo.getSdks(toolPath).toList()

        // Then
        Assert.assertEquals(
                actualSdks,
                listOf(
                        DotnetSdk(File(File("dotnet", "sdk"), "1.2.3"), Version(1, 2,3)),
                        DotnetSdk(File(File("dotnet", "sdk"), "1.2.3-rc"), Version.parse("1.2.3-rc")),
                        DotnetSdk(File(File("dotnet", "sdk"), "1.2.5"), Version(1, 2,5))
                ))
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetSdksProviderImpl(fileSystemService)

    companion object {
        private val sdkPath = File(File(File("Program Files"), "dotnet"), "sdk")
    }
}