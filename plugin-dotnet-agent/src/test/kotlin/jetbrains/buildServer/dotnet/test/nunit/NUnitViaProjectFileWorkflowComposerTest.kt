package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.test.StringExtensions.toPlatformPath
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.arguments.NUnitArgumentsProvider
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.nUnitProject.NUnitProject
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectGenerator
import jetbrains.buildServer.nunit.nUnitProject.NUnitProjectSerializer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Paths

class NUnitViaProjectFileWorkflowComposerTest {
    @MockK
    private lateinit var _nUnitArgumentsProvider: NUnitArgumentsProvider

    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _fileSystem: FileSystemService

    @MockK
    private lateinit var _nUnitProjectSerializer: NUnitProjectSerializer

    @MockK
    private lateinit var _nUnitConsoleRunnerPathProvider: NUnitConsoleRunnerPathProvider

    @MockK
    private lateinit var _nUnitProjectGenerator: NUnitProjectGenerator

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should compose nunit project command line command`() {
        // arrange
        val consolePath = "/nunit-dir/nunit3-console.exe".toPlatformPath()
        every { _nUnitConsoleRunnerPathProvider.consoleRunnerPath } returns Paths.get(consolePath)

        val workDir = "/work-dir".toPlatformPath()
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File(workDir)
        every { _pathsService.getPath(PathType.Checkout) } returns File("checkout-dir")

        val project1 = NUnitProject(appBase = File("project1"), testingAssemblies = listOf())
        val project2 = NUnitProject(appBase = File("project2"), testingAssemblies = listOf())
        every { _nUnitProjectGenerator.generate() } returns listOf(project1, project2)

        _nUnitProjectSerializer.let {
            every { it.create(project1, any()) } answers {
                arg<ByteArrayOutputStream>(1).run {
                    write("project1-content".encodeToByteArray())
                    close()
                }
            }
            every { it.create(project2, any()) } answers {
                arg<ByteArrayOutputStream>(1).run {
                    write("project2-content".encodeToByteArray())
                    close()
                }
            }
        }

        every { _pathsService.getTempFileName(".nunit") } returnsMany listOf(File("1.nunit"), File("2.nunit"))

        _nUnitArgumentsProvider.let {
            every { it.createCommandLineArguments(File("1.nunit.xml")) } returns
                    sequenceOf(CommandLineArgument("--project1=arg"))

            every { it.createCommandLineArguments(File("2.nunit.xml")) } returns
                    sequenceOf(CommandLineArgument("--project2=arg"))
        }


        justRun { _loggerService.writeMessage(any()) }
        justRun { _fileSystem.write(any(), any()) }

        val composer = NUnitViaProjectFileWorkflowComposer(
            _nUnitArgumentsProvider,
            _pathsService,
            _fileSystem,
            _loggerService,
            _nUnitConsoleRunnerPathProvider,
            _nUnitProjectSerializer,
            _nUnitProjectGenerator
        )

        // act
        val commands = composer.compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        Assert.assertEquals(commands.size, 2)

        Assert.assertEquals(commands[0].executableFile.path, consolePath)
        Assert.assertEquals(commands[1].executableFile.path, consolePath)

        Assert.assertEquals(commands[0].workingDirectory.path, workDir)
        Assert.assertEquals(commands[1].workingDirectory.path, workDir)

        Assert.assertEquals(
            commands[0].arguments,
            listOf(CommandLineArgument("1.nunit"), CommandLineArgument("--project1=arg"))
        )
        Assert.assertEquals(
            commands[1].arguments,
            listOf(CommandLineArgument("2.nunit"), CommandLineArgument("--project2=arg"))
        )

        verify(exactly = 2) { _loggerService.writeMessage(match { it.messageName == "publishArtifacts" }) }
    }
}