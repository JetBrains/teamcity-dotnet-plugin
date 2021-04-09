package jetbrains.buildServer.dotnet.test.visualStudio

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
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.visualStudio.VisualStudioInstanceFactory
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioInstanceFactoryTest {
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
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(15, 3, 0), Version.parse("2017"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(14, 0, 1, 3),
                        File("path"),
                        Version(14, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(14, 0, 1, 3), Version.parse("2015"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(12, 1, 2),
                        File("path"),
                        Version(12, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(12, 1, 2), Version.parse("2013"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(11),
                        File("path"),
                        Version(11, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(11), Version.parse("2012"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(10, 0),
                        File("path"),
                        Version(10, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(10, 0), Version.parse("2010"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(9, 0, 0),
                        File("path"),
                        Version(9, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(9, 0, 0), Version.parse("2008"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(8, 0, 1),
                        File("path"),
                        Version(8, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(8, 0, 1), Version.parse("2005"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(7, 1, 2),
                        File("path"),
                        Version(7, 1),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(7, 1, 2), Version.parse("2003"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(11, 0, 7),
                        File("path"),
                        Version.Empty,
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(11, 0, 7), Version.parse("2012"), Platform.Default)
                ),
                // Cannot get a product version
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version.Empty,
                        File("path"),
                        Version(11, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(11, 0), Version.parse("2012"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(11, 0, 1),
                        File("path"),
                        Version(11, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(11, 0, 1), Version.parse("2012"), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(11, 0, 1),
                        File("path"),
                        Version(11, 0),
                        ToolInstance(ToolInstanceType.VisualStudio, File("path"), Version(11, 0, 1), Version.parse("2012"), Platform.Default)
                ),
                // Cannot find a product line version
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv.exe")),
                        Version(16, 0, 1),
                        File("path"),
                        Version(16, 0),
                        null
                ),
                // Cannot find devenv.exe
                arrayOf(
                        VirtualFileSystemService().addFile(File(File("path"), "devenv2.exe")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        null
                ),
                // devenv.exe is a directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(File("path"), "devenv.exe")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        null
                ),
                // Empty base directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(File("path")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        null
                ),
                // Empty base is a file
                arrayOf(
                        VirtualFileSystemService().addFile(File("path")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        null
                ),
                // Empty base directory does not exist
                arrayOf(
                        VirtualFileSystemService(),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
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
        every { _peReader.tryGetVersion(File(path, "devenv.exe")) } returns peVersion
        val actualInstance = factory.tryCreate(path, baseVersion, Platform.Default)

        // Then
        Assert.assertEquals(actualInstance, expectedInstance)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            VisualStudioInstanceFactory(fileSystemService, _peReader)
}