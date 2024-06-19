package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.test.StringExtensions.toPlatformPath
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.arguments.NUnitArgumentsProvider
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.arguments.NUnitTestingAssembliesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Paths

class NUnitViaCommandLineWorkflowComposerTest {
    @MockK
    private lateinit var _nUnitArgumentsProvider: NUnitArgumentsProvider

    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _nUnitConsoleRunnerPathProvider: NUnitConsoleRunnerPathProvider

    @MockK
    private lateinit var _nUnitTestingAssembliesProvider: NUnitTestingAssembliesProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should compose command line test command`() {
        // arrange
        every { _nUnitTestingAssembliesProvider.assemblies } returns listOf(
            File("assembly1.dll"),
            File("assembly2.dll")
        )
        val consolePath = "/nunit-dir/nunit3-console.exe".toPlatformPath()
        every { _nUnitConsoleRunnerPathProvider.consoleRunnerPath } returns Paths.get(consolePath)
        val workDir = "/work-dir".toPlatformPath()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File(workDir)
        every { _nUnitSettings.appConfigFile } returns "test.config"
        val resultsFile = File("results.xml")
        every { _pathsService.getTempFileName(".xml") } returns resultsFile
        every { _nUnitArgumentsProvider.createCommandLineArguments(resultsFile) } returns sequenceOf(
            CommandLineArgument("--arg1=test"),
            CommandLineArgument("--arg2"),
        )

        val composer = NUnitViaCommandLineWorkflowComposer(
            _nUnitArgumentsProvider,
            _pathsService,
            _nUnitSettings,
            _nUnitConsoleRunnerPathProvider,
            _nUnitTestingAssembliesProvider
        )

        // act
        val commands = composer.compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        Assert.assertEquals(commands.size, 1)
        Assert.assertEquals(commands[0].executableFile.path, consolePath)
        Assert.assertEquals(commands[0].workingDirectory.path, workDir)
        Assert.assertEquals(commands[0].arguments, listOf(
            CommandLineArgument("assembly1.dll"),
            CommandLineArgument("assembly2.dll"),
            CommandLineArgument("--configfile=test.config"),
            CommandLineArgument("--arg1=test"),
            CommandLineArgument("--arg2"),
        ))
    }
}