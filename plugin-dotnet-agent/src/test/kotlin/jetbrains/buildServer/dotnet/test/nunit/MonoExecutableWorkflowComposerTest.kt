package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.CannotExecute
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.ToolState
import jetbrains.buildServer.dotnet.ToolStateWorkflowComposer
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.mono.MonoToolProvider
import jetbrains.buildServer.nunit.MonoExecutableWorkflowComposer
import jetbrains.buildServer.util.OSType
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class MonoExecutableWorkflowComposerTest {
    @MockK
    private lateinit var _buildStepContext: BuildStepContext

    @MockK
    private lateinit var _virtualContext: VirtualContext

    @MockK
    private lateinit var _cannotExecute: CannotExecute

    @MockK
    private lateinit var _monoToolProvider: MonoToolProvider

    @MockK
    private lateinit var _toolStateWorkflowComposer: ToolStateWorkflowComposer

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _buildStepContext.runnerContext } returns mockk<BuildRunnerContext>() {
            every { build } returns mockk<AgentRunningBuild>()
        }
        every { _monoToolProvider.getPath(any(), any(), any()) } returns "mono"
    }

    @Test
    fun `should not modify mono commands on windows`() {
        // arrange
        every { _virtualContext.targetOSType } returns OSType.WINDOWS
        val workflow = Workflow(sequenceOf(createNUnitToolCommand()))

        // act
        val commands = createComposer()
            .compose(WorkflowContextStub(WorkflowStatus.Running), Unit, workflow)
            .commandLines.toList()

        // assert
        assertEquals(commands.size, 1)
        assertEquals(commands[0], createNUnitToolCommand())
    }

    @Test
    fun `should report build problem when mono is not available on linux`() {
        // arrange
        every { _virtualContext.targetOSType } returns OSType.UNIX
        every { _monoToolProvider.getPath(any(), any(), any()) } throws Exception("no mono")
        justRun { _cannotExecute.writeBuildProblemFor(any()) }
        val workflow = Workflow(sequenceOf(createNUnitToolCommand()))

        // act
        val commands = createComposer()
            .compose(WorkflowContextStub(WorkflowStatus.Running), Unit, workflow)
            .commandLines.toList()

        // assert
        verify { _cannotExecute.writeBuildProblemFor(any()) }
        assertEquals(commands.size, 0)
    }

    @Test
    fun `should compose nunit console with mono on linux`() {
        // arrange
        every { _virtualContext.targetOSType } returns OSType.UNIX
        every { _virtualContext.isVirtual } returns false
        val workflow = Workflow(sequenceOf((createNUnitToolCommand())))

        // act
        val commands = createComposer()
            .compose(WorkflowContextStub(WorkflowStatus.Running), Unit, workflow)
            .commandLines.toList()

        // assert
        assertEquals(commands.size, 1)
        commands[0].let {
            assertEquals(
                it.arguments, listOf(
                    CommandLineArgument("nunit3-console.exe", CommandLineArgumentType.Target),
                    CommandLineArgument("--nunit-arg")
                )
            )
            assertEquals(it.executableFile.path, "mono")
            assertEquals(it.workingDirectory, createNUnitToolCommand().workingDirectory)
        }
    }

    @Test
    fun `should resolve full path to mono in linux container`() {
        // arrange
        val resolvedMonoPath = "/usr/bin/mono"
        every { _virtualContext.targetOSType } returns OSType.UNIX
        every { _virtualContext.isVirtual } returns true
        val workflow = Workflow(sequenceOf((createNUnitToolCommand())))

        every { _toolStateWorkflowComposer.compose(any(), any(), any()) } answers {
            arg<ToolState>(1).virtualPathObserver.onNext(Path(resolvedMonoPath))
            Workflow()
        }

        // act
        val commands = createComposer()
            .compose(WorkflowContextStub(WorkflowStatus.Running), Unit, workflow)
            .commandLines.toList()

        // assert
        assertEquals(commands.size, 1)
        commands[0].let {
            assertEquals(it.executableFile.path, resolvedMonoPath)
            assertEquals(
                it.arguments, listOf(
                    CommandLineArgument("nunit3-console.exe", CommandLineArgumentType.Target),
                    CommandLineArgument("--nunit-arg")
                )
            )
            assertEquals(it.workingDirectory, createNUnitToolCommand().workingDirectory)
        }
    }

    private fun createNUnitToolCommand() = CommandLine(
        baseCommandLine = null,
        target = TargetType.Tool,
        executableFile = Path("nunit3-console.exe"),
        workingDirectory = Path("wd"),
        arguments = listOf(CommandLineArgument("--nunit-arg"))
    )

    private fun createComposer() = MonoExecutableWorkflowComposer(
        _buildStepContext,
        _virtualContext,
        _cannotExecute,
        _monoToolProvider,
        _toolStateWorkflowComposer
    )
}