package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.testReordering.*
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class NUnitReorderingWorkflowComposerTest {
    @MockK
    private lateinit var _nUnitXmlTestInfoParser: NUnitXmlTestInfoParser

    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _fileSystem: FileSystemService

    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _reorderingTestsSplitService: NUnitReorderingTestsSplitService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should reorder test execution to run recently failed tests first`() {
        // arrange
        val recentlyFailedTests = listOf(
            "FailedTestClassFromProject2"
        ).map { TestInfo(it) }

        val project1TestClassList = listOf(
            "Test1",
            "Test2",
            "Test3"
        ).map { TestInfo(it) }

        val project2TestClassList = listOf(
            "Test4",
            "Test5",
            "Test6",
            "FailedTestClassFromProject2"
        ).map { TestInfo(it) }

        val project1WorkDir = "work-dir1"
        val project2WorkDir = "work-dir2"

        val expectedProject1FirstRun = emptyList<TestInfo>()
        val expectedProject1SecondRun = listOf("Test1", "Test2", "Test3").map { TestInfo(it) }

        val expectedProject2FirstRun = listOf("FailedTestClassFromProject2").map { TestInfo(it) }
        val expectedProject2SecondRun = listOf("Test4", "Test5", "Test6").map { TestInfo(it) }

        val workflowContext = WorkflowContextStub(WorkflowStatus.Running)
        val project1TestListFile = File("1_all.tests")
        val project2TestListFile = File("2_all.tests")
        _pathsService.let {
            every { it.getTempFileName("_all.tests") } returnsMany listOf(
                project1TestListFile,
                project2TestListFile
            )
            every { it.getTempFileName("_step1.tests") } returnsMany listOf(
                File("1_step1.tests"),
                File("2_step1.tests")
            )
            every { it.getTempFileName("_step2.tests") } returnsMany listOf(
                File("1_step2.tests"),
                File("2_step2.tests")
            )
        }


        every { _nUnitSettings.testReorderingRecentlyFailedTests } returns recentlyFailedTests
        val project1TestListXml = "xml-test-list-project-1"
        val project2TestListXml = "xml-test-list-project-2"
        _fileSystem.let {
            every { it.read<String>(project1TestListFile, any()) } returns project1TestListXml
            every { it.read<String>(project2TestListFile, any()) } returns project2TestListXml
            justRun { it.write(any(), any()) }
        }
        _nUnitXmlTestInfoParser.let {
            every { it.parse(project1TestListXml) } returns project1TestClassList
            every { it.parse(project2TestListXml) } returns project2TestClassList
        }

        _reorderingTestsSplitService.let {
            every { it.splitTests(project1TestClassList, recentlyFailedTests) } returns
                    TestsSplit(expectedProject1FirstRun, expectedProject1SecondRun)
            every { it.splitTests(project2TestClassList, recentlyFailedTests) } returns
                    TestsSplit(expectedProject2FirstRun, expectedProject2SecondRun)
        }

        justRun { _loggerService.writeMessage(any()) }

        val composer = NUnitReorderingWorkflowComposer(
            _nUnitXmlTestInfoParser,
            _nUnitSettings,
            _fileSystem,
            _pathsService,
            _loggerService,
            _reorderingTestsSplitService
        )

        val workflow = Workflow(
            sequenceOf(
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.Tool,
                    executableFile = Path("nunit3-console.exe"),
                    workingDirectory = Path(project1WorkDir),
                    arguments = listOf(CommandLineArgument("--project1=arg"))
                ),
                CommandLine(
                    baseCommandLine = null,
                    target = TargetType.Tool,
                    executableFile = Path("nunit3-console.exe"),
                    workingDirectory = Path(project2WorkDir),
                    arguments = listOf(CommandLineArgument("--project2=arg"))
                )
            )
        )

        // act
        val commands = composer.compose(workflowContext, Unit, workflow).commandLines.toList()

        // assert
        assertEquals(commands.size, 5)

        // 2 explore commands
        commands[0].let {
            assertEquals(it.target, TargetType.AuxiliaryTool)
            assertEquals(it.workingDirectory.path, project1WorkDir)
            assertEquals(it.arguments, listOf(
                CommandLineArgument("--project1=arg"),
                CommandLineArgument("--explore=1_all.tests"))
            )
        }

        commands[1].let {
            assertEquals(it.target, TargetType.AuxiliaryTool)
            assertEquals(it.workingDirectory.path, project2WorkDir)
            assertEquals(it.arguments, listOf(
                CommandLineArgument("--project2=arg"),
                CommandLineArgument("--explore=2_all.tests"))
            )
        }
        // 1 recently failed tests run from project2
        commands[2].let {
            assertEquals(it.target, TargetType.Tool)
            assertEquals(it.workingDirectory.path, project2WorkDir)
            assertEquals(it.arguments, listOf(
                CommandLineArgument("--project2=arg"),
                CommandLineArgument("--testlist=1_step1.tests"))
            )
        }

        // 2 runs of remaining tests from both projects
        commands[3].let {
            assertEquals(it.target, TargetType.Tool)
            assertEquals(it.workingDirectory.path, project1WorkDir)
            assertEquals(it.arguments, listOf(
                CommandLineArgument("--project1=arg"),
                CommandLineArgument("--testlist=1_step2.tests"))
            )
        }

        commands[4].let {
            assertEquals(it.target, TargetType.Tool)
            assertEquals(it.workingDirectory.path, project2WorkDir)
            assertEquals(it.arguments, listOf(
                CommandLineArgument("--project2=arg"),
                CommandLineArgument("--testlist=2_step2.tests"))
            )
        }


        // verify all test lists are published
        listOf("1_all.tests", "2_all.tests", "1_step2.tests", "1_step1.tests", "2_step2.tests")
            .forEach { artifactName ->
                verify(exactly = 1) {
                    _loggerService.writeMessage(match {
                        it.messageName == "publishArtifacts" && it.argument!!.contains(
                            artifactName
                        )
                    })
                }
            }

    }
}