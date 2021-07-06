package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetRuntime
import jetbrains.buildServer.dotnet.DotnetRuntimesProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.script.AnyVersionResolverImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class AnyVersionResolverTest {
    @MockK private lateinit var _runtimesProvider: DotnetRuntimesProvider
    private val DefaultToolsPath = File("tools")
    private val DotnetPath = "dotnet"

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        File(DefaultToolsPath, "net5.0")),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(3, 1), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        File(DefaultToolsPath, "netcoreapp3.1")),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net6.0")),
                        DefaultToolsPath,
                        null),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0, 0, "-beta"), "")),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        File(DefaultToolsPath, "net5.0")),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(6, 0, 0,"beta"), ""),
                                DotnetRuntime(File("."), Version(3, 1), "")),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "net6.0"))
                                .addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        File(DefaultToolsPath, "net6.0")),

                arrayOf(
                        sequenceOf(
                                DotnetRuntime(File("."), Version(5, 0), ""),
                                DotnetRuntime(File("."), Version(3, 1), "")),
                        VirtualFileSystemService()
                                .addDirectory(File(DefaultToolsPath, "net5.0"))
                                .addDirectory(File(DefaultToolsPath, "netcoreapp3.1")),
                        DefaultToolsPath,
                        File(DefaultToolsPath, "net5.0")),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService().addFile(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        null),

                arrayOf(
                        sequenceOf(DotnetRuntime(File("."), Version(5, 0), "")),
                        VirtualFileSystemService(),
                        DefaultToolsPath,
                        null),

                arrayOf(
                        emptySequence<DotnetRuntime>(),
                        VirtualFileSystemService().addDirectory(File(DefaultToolsPath, "net5.0")),
                        DefaultToolsPath,
                        null)
        )
    }

    @Test(dataProvider = "cases")
    fun shouldResolve(
            runtimes: Sequence<DotnetRuntime>,
            fileSystemService: FileSystemService,
            toolsPath: File,
            expectedVersionPath: File?) {
        // Given
        every { _runtimesProvider.getRuntimes() }.returns(runtimes)
        val resoler = createInstance(fileSystemService)

        // When
        var actualVersionPath: File? = null
        try {
            actualVersionPath = resoler.resolve(toolsPath)
        }
        catch(ex: Exception) { }

        // Then
        Assert.assertEquals(actualVersionPath, expectedVersionPath)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            AnyVersionResolverImpl(fileSystemService, _runtimesProvider)
}