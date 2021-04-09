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
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.VisualStudioTestConsoleInstanceFactory
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioTestConsoleInstanceFactoryTest {
    @MockK private lateinit var _peReader: PEReader
    private val _basePath = File(File(File(File("path"), "CommonExtensions"), "Microsoft"), "TestWindow")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService().addFile(File(_basePath, "vstest.console.exe")),
                        Version(15, 3, 1),
                        File("path"),
                        Version(20, 0),
                        ToolInstance(ToolInstanceType.VisualStudioTest, File(_basePath, "vstest.console.exe"), Version(15, 3, 1), Version(15, 3, 0), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("path", "vstest.console.exe")),
                        Version(15, 3, 1),
                        File("path"),
                        Version(20, 0),
                        ToolInstance(ToolInstanceType.VisualStudioTest, File("path", "vstest.console.exe"), Version(15, 3, 1), Version(15, 3, 0), Platform.Default)
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(_basePath, "vstest.console.exe")).addFile(File("path", "vstest.console.exe")),
                        Version(15, 3, 1),
                        File("path"),
                        Version(20, 0),
                        ToolInstance(ToolInstanceType.VisualStudioTest, File("path", "vstest.console.exe"), Version(15, 3, 1), Version(15, 3, 0), Platform.Default)
                ),
                // Cannot find vstest.console.exe
                arrayOf(
                        VirtualFileSystemService().addFile(File(_basePath, "vstest.console2.exe")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(15, 0),
                        null
                ),
                // vstest.console.exe is a directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(_basePath, "vstest.console.exe")),
                        Version(15, 3, 0),
                        File("path"),
                        Version(20, 0),
                        null
                ),
                // Empty base directory
                arrayOf(
                        VirtualFileSystemService().addDirectory(_basePath),
                        Version(15, 3, 0),
                        File("path"),
                        Version(20, 0),
                        null
                ),
                // Empty base is a file
                arrayOf(
                        VirtualFileSystemService().addFile(_basePath),
                        Version(15, 3, 0),
                        File("path"),
                        Version(20, 0),
                        null
                ),
                // Empty base directory does not exist
                arrayOf(
                        VirtualFileSystemService(),
                        Version(15, 3, 0),
                        File("path"),
                        Version(20, 0),
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
        every { _peReader.tryGetVersion(File(_basePath, "vstest.console.exe")) } returns peVersion
        every { _peReader.tryGetVersion(File("path", "vstest.console.exe")) } returns peVersion
        val actualInstance = factory.tryCreate(path, baseVersion, Platform.Default)

        // Then
        Assert.assertEquals(actualInstance, expectedInstance)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            VisualStudioTestConsoleInstanceFactory(fileSystemService, _peReader)
}