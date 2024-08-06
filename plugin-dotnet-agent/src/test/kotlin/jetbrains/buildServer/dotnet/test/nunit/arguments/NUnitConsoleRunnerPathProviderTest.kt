package jetbrains.buildServer.dotnet.test.nunit.arguments

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.test.StringExtensions.toPlatformPath
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.NUnitSettings
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class NUnitConsoleRunnerPathProviderTest {
    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }


    data class TestCase(
        val expectedPath: String,
        val nunitPathParameter: String,
        val existingFileSystemPaths: List<String>
    )

    @DataProvider(name = "testCases")
    fun getCases(): Array<TestCase> = arrayOf(
        TestCase(
            expectedPath = "/path/to/custom/executable.exe",
            nunitPathParameter = "/path/to/custom/executable.exe",
            existingFileSystemPaths = listOf("/path/to/custom/executable.exe")
        ),
        TestCase(
            expectedPath = "/checkout/dir/bin/net35/nunit3-console.exe",
            nunitPathParameter = "/checkout/dir/",
            existingFileSystemPaths = listOf(
                "/checkout/dir/bin/net20/nunit3-console.exe",
                "/checkout/dir/bin/nunit3-console.exe",
                "/checkout/dir/bin/net35/nunit3-console.exe"
            )
        ),
        TestCase(
            expectedPath = "/checkout/dir/bin/net20/nunit3-console.exe",
            nunitPathParameter = "/checkout/dir/",
            existingFileSystemPaths = listOf(
                "/checkout/dir/bin/nunit3-console.exe",
                "/checkout/dir/bin/net20/nunit3-console.exe"
            )
        ),
        TestCase(
            expectedPath = "/checkout/dir/bin/nunit3-console.exe",
            nunitPathParameter = "/checkout/dir/",
            existingFileSystemPaths = listOf(
                "/checkout/dir/bin/nunit3-console.exe",
                "/checkout/dir/bin/nunit42-console.exe"
            )
        ),
        TestCase(
            expectedPath = "/checkout/dir/bin/net462/nunit3-console.exe",
            nunitPathParameter = "/checkout/dir/",
            existingFileSystemPaths = listOf(
                "/checkout/dir/bin/nunit3-console.exe",
                "/checkout/dir/bin/net20/nunit3-console.exe",
                "/checkout/dir/bin/net35/nunit3-console.exe",
                "/checkout/dir/bin/net462/nunit3-console.exe",
            )
        ),
    )

    @Test(dataProvider = "testCases")
    fun `should resolve path to nunit console executable`(testCase: TestCase) {
        // arrange
        val absoluteNUnitPath = Paths.get(testCase.nunitPathParameter.toPlatformPath())

        every { _nUnitSettings.nUnitPath } returns "nunit-path"
        every { _pathsService.resolvePath(PathType.Checkout, "nunit-path") } returns absoluteNUnitPath
        justRun { _loggerService.writeDebug(any()) }

        val fileSystem = VirtualFileSystemService()
        testCase.existingFileSystemPaths.forEach {
            fileSystem.addFile(File(it.toPlatformPath()))
        }
        val provider = NUnitConsoleRunnerPathProvider(
            _nUnitSettings, fileSystem, _pathsService, _loggerService
        )

        // act
        val consoleExecutable = provider.consoleRunnerPath

        // assert
        Assert.assertEquals(consoleExecutable.absolutePathString(), testCase.expectedPath.toPlatformPath())
    }

    @Test
    fun `should throw when nunit console executable is not found`() {
        // arrange
        val absoluteNUnitPath = Paths.get("/checkout/dir".toPlatformPath())
        every { _nUnitSettings.nUnitPath } returns "nunit-path"
        every { _pathsService.resolvePath(PathType.Checkout, "nunit-path") } returns absoluteNUnitPath
        justRun { _loggerService.writeDebug(any()) }

        val fileSystem = VirtualFileSystemService()
            .addFile(File("/checkout/dir/some/random/file1.exe".toPlatformPath()))
            .addFile(File("/checkout/dir/some/random/file2.exe".toPlatformPath()))
        val provider = NUnitConsoleRunnerPathProvider(
            _nUnitSettings, fileSystem, _pathsService, _loggerService
        )

        // act, assert
        Assert.assertThrows(RunBuildException::class.java) { provider.consoleRunnerPath }
    }
}