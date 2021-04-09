package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.dotnet.DotnetFrameworkSdkInstanceFactory
import jetbrains.buildServer.dotnet.Platform
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworkSdkInstanceFactoryTest {
    @MockK private lateinit var _peReader: PEReader

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "wsdl.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 1, 123), Version.parse("1.1"), Platform.x86)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File("path"), "Bin"), "wsdl.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 1, 123), Version.parse("1.1"), Platform.x86)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File("path"), "Bin"), "wsdl.exe")).addFile(File(File("path"), "wsdl.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 1, 123), Version.parse("1.1"), Platform.x86)
                ),
                // Cannot find a product line version
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File("path"), "Bin"), "wsdl.exe")),
                        Version.Empty,
                        File("path"),
                        Version(1, 1),
                        ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path"), Version(1, 1), Version.parse("1.1"), Platform.x86)
                ),
                // Cannot find wsdl.exe
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File("path"), "Bin"), "wsdl2.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File("path"), "Bin2"), "wsdl.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                ),
                // wsdl.exe is a directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(File("path"), "wsdl.exe")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                ),
                // Empty base directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(File("path")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                ),
                // Empty base is a file
                arrayOf(
                        VirtualFileSystemService().addFile(File("path")),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                ),
                // Empty base directory does not exist
                arrayOf(
                        VirtualFileSystemService(),
                        Version(1, 1, 123),
                        File("path"),
                        Version(1, 1),
                        null
                )
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldCreateInstance(
            fileSystemService: FileSystemService,
            peVersion: Version,
            path: File,
            baseVersion: Version,
            expectedInstance: ToolInstance?) {
        // Given
        val factory = createInstance(fileSystemService)

        // When
        every { _peReader.tryGetVersion(File(path, "wsdl.exe")) } returns peVersion
        every { _peReader.tryGetVersion(File(File(path, "Bin"), "wsdl.exe")) } returns peVersion
        val actualInstance = factory.tryCreate(path, baseVersion, Platform.x86)

        // Then
        Assert.assertEquals(actualInstance, expectedInstance)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            DotnetFrameworkSdkInstanceFactory(fileSystemService, _peReader)
}