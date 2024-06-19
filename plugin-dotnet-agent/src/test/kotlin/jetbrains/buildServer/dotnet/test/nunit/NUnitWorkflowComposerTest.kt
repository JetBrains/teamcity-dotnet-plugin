package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.NUnitReorderingWorkflowComposer
import jetbrains.buildServer.nunit.testReordering.TestInfo
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateVerifier
import jetbrains.buildServer.rx.Observer
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class NUnitWorkflowComposerTest {
    @MockK
    private lateinit var _buildStepContext: BuildStepContext

    @MockK
    private lateinit var _loggerService: LoggerService

    @MockK
    private lateinit var _nUnitSettings: NUnitSettings

    @MockK
    private lateinit var _nUnitToolStateVerifier: NUnitToolStateVerifier

    @MockK
    private lateinit var _nUnitToolStateWorkflowComposer: NUnitToolStateWorkflowComposer

    @MockK
    private lateinit var _nUnitViaCommandLineComposer: NUnitViaCommandLineWorkflowComposer

    @MockK
    private lateinit var _nUnitViaProjectFileComposer: NUnitViaProjectFileWorkflowComposer

    @MockK
    private lateinit var _nUnitTestReorderingComposer: NUnitReorderingWorkflowComposer

    @MockK
    private lateinit var _monoExecutableWorkflowComposer: MonoExecutableWorkflowComposer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _buildStepContext.runnerContext.runType } returns NUnitRunnerConstants.NUNIT_RUN_TYPE

        val nUnitToolState = NUnitToolState("3.0.0", listOf())
        justRun { _nUnitToolStateVerifier.verify(nUnitToolState) }

        every { _nUnitToolStateWorkflowComposer.compose(any(), any(), any()) } answers {
            arg<Observer<NUnitToolState>>(1).onNext(nUnitToolState)
            Workflow(
                sequenceOf(
                    CommandLine(null, TargetType.AuxiliaryTool, Path("nunit.exe"), Path("workdir"), emptyList())
                )
            )
        }

        every { _monoExecutableWorkflowComposer.compose(any(), Unit, any()) } answers { lastArg<Workflow>() }
    }

    @Test
    fun `should return if not nunit runner`() {
        // arrange
        every { _buildStepContext.runnerContext.runType } returns "NOT_NUNIT_RUN_TYPE"

        val composer = createComposer()

        // act
        val commands = composer.compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        assertEquals(commands.size, 0)
    }

    @Test
    fun `should use nunit command line composer`() {
        // arrange
        _nUnitSettings.let {
            every { it.testReorderingEnabled } returns false
            every { it.useProjectFile } returns false
        }

        every { _nUnitViaCommandLineComposer.compose(any(), Unit, any()) } returns Workflow(
            sequenceOf(
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList()),
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList())
            )
        )

        // act
        val commands = createComposer().compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        assertEquals(commands.size, 3)
        assertEquals(commands[0].target, TargetType.AuxiliaryTool)
        assertEquals(commands[1].target, TargetType.Tool)
        assertEquals(commands[2].target, TargetType.Tool)

        verify(exactly = 1) { _nUnitToolStateVerifier.verify(any()) }
    }

    @Test
    fun `should use nunit project composer`() {
        // arrange
        _nUnitSettings.let {
            every { it.testReorderingEnabled } returns false
            every { it.useProjectFile } returns true
        }

        every { _nUnitViaProjectFileComposer.compose(any(), Unit, any()) } returns Workflow(
            sequenceOf(
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList()),
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList())
            )
        )

        // act
        val commands = createComposer().compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        assertEquals(commands.size, 3)
    }

    @Test
    fun `should use nunit test reordering composer`() {
        // arrange
        _nUnitSettings.let {
            every { it.testReorderingEnabled } returns true
            every { it.testReorderingRecentlyFailedTests } returns listOf(TestInfo("TestClass1"))
            every { it.useProjectFile } returns true
        }

        val viaProjectWorkflow = Workflow()
        every { _nUnitViaProjectFileComposer.compose(any(), Unit, any()) } returns viaProjectWorkflow
        every { _nUnitTestReorderingComposer.compose(any(), Unit, viaProjectWorkflow) } returns Workflow(
            sequenceOf(
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList()),
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList())
            )
        )

        // act
        val commands = createComposer().compose(WorkflowContextStub(WorkflowStatus.Running), Unit).commandLines.toList()

        // assert
        assertEquals(commands.size, 3)
    }

    @Test
    fun `should abort on negative exit code`() {
        // arrange
        _nUnitSettings.let {
            every { it.testReorderingEnabled } returns false
            every { it.useProjectFile } returns false
        }

        every { _nUnitViaCommandLineComposer.compose(any(), Unit, any()) } returns Workflow(
            sequenceOf(
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList()),
                CommandLine(null, TargetType.Tool, Path("nunit.exe"), Path("workdir"), emptyList())
            )
        )

        _loggerService.let {
            justRun { it.writeErrorOutput(any()) }
            justRun { it.writeBuildProblem(any(), any(), any()) }
        }

        every { _buildStepContext.runnerContext.build.failBuildOnExitCode } returns true

        val workflowContext = WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(-42))

        // act
        val commands = createComposer().compose(workflowContext, Unit).commandLines.toList()

        // assert
        assertEquals(commands.size, 2)
        assertEquals(commands[0].target, TargetType.AuxiliaryTool)
        assertEquals(commands[1].target, TargetType.Tool)
        assertEquals(workflowContext.status, WorkflowStatus.Failed)

        _loggerService.let {
            verify(exactly = 1) { it.writeErrorOutput(any()) }
            verify(exactly = 1) { it.writeBuildProblem(any(), any(), any()) }
        }
    }

    private fun createComposer() = NUnitWorkflowComposer(
        _buildStepContext,
        _loggerService,
        _nUnitSettings,
        _nUnitToolStateVerifier,
        _nUnitToolStateWorkflowComposer,
        _nUnitViaCommandLineComposer,
        _nUnitViaProjectFileComposer,
        _nUnitTestReorderingComposer,
        _monoExecutableWorkflowComposer
    )
}