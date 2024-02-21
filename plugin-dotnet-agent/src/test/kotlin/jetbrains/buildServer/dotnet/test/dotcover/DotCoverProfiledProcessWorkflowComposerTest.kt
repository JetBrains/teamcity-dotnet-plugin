package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.DotCoverProfiledProcessWorkflowComposer
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverProfiledProcessWorkflowComposerTest {
    @MockK private lateinit var _buildInfoMock: BuildInfo
    @MockK private lateinit var _parametersServiceMock: ParametersService
    @MockK private lateinit var _pathsServiceMock: PathsService
    @MockK private lateinit var _argumentsServiceMock: ArgumentsService
    @MockK private lateinit var _buildOptionsMock: BuildOptions
    @MockK private lateinit var _loggerServiceMock: LoggerService
    @MockK private lateinit var _virtualContextMock: VirtualContext
    private lateinit var _instance: DotCoverProfiledProcessWorkflowComposer

    @BeforeMethod
    fun setup() {
        clearAllMocks()
        MockKAnnotations.init(this, relaxed = true)
        every { _buildInfoMock.runType } returns CoverageConstants.DOTCOVER_RUNNER_TYPE
        _instance = DotCoverProfiledProcessWorkflowComposer(
            _buildInfoMock,
            _parametersServiceMock,
            _pathsServiceMock,
            _argumentsServiceMock,
            _buildOptionsMock,
            _loggerServiceMock,
            _virtualContextMock
        )
    }

    @Test
    fun `should be Tool target type`() {
        // act, assert
        assertEquals(_instance.target, TargetType.Tool)
    }

    @Test
    fun `should return empty workflow when runner type is not dotCover runner`() {
        // arrange
        every { _buildInfoMock.runType } returns "UNKNOWN"

        // act
        val result = _instance.compose(mockk(), mockk(), mockk())

        // assert
        assertEquals(result, Workflow())
    }

    @DataProvider
    fun `empty executable parameter`() = arrayOf(
        arrayOf<String?>(null),
        arrayOf(""),
        arrayOf("  ")
    )
    @Test(dataProvider = "empty executable parameter")
    fun `should return empty workflow when executable parameter is empty`(emptyExecutable: String?) {
        // arrange
        _parametersServiceMock.also {
            every { it.tryGetParameter(any(), any()) } returns emptyExecutable
        }

        // act
        val result = _instance.compose(mockk(), mockk(), mockk())

        // assert
        assertEquals(result, Workflow())
    }

    @Test
    fun `should return workflow with single command line when executable is present`() {
        // arrange
        val workingDirectory = "workingDirectory"
        val executable = "abc"
        val args = "x y z"
        val splitArgs = args.split(" ")
        _parametersServiceMock.also {
            every { it.tryGetParameter(any(), CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE) } returns executable
            every { it.tryGetParameter(any(), CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_ARGUMENTS) } returns args
        }
        _argumentsServiceMock.also {
            every { it.split(any()) } returns splitArgs.asSequence()
        }
        every { _virtualContextMock.resolvePath(any()) } returns executable
        every { _pathsServiceMock.getPath(any()) } returns File(workingDirectory)
        val workflowContexMock = mockk<WorkflowContext>(relaxed = true)

        // act
        val resultCmds = _instance.compose(workflowContexMock, mockk(), mockk()).commandLines.toList()

        // assert
        assertEquals(resultCmds.size, 1)
        val resultCmd = resultCmds.first()
        assertEquals(resultCmd.target, TargetType.Tool)
        assertEquals(resultCmd.executableFile.path, executable)
        assertEquals(resultCmd.workingDirectory.path, workingDirectory)
        assertEquals(resultCmd.arguments.count(), splitArgs.count())
        resultCmd.arguments.toList().forEach {
            assertTrue(splitArgs.contains(it.value))
            assertEquals(CommandLineArgumentType.Custom, it.argumentType)
        }
        verify(exactly = 1) { _virtualContextMock.resolvePath(executable) }
        verify(exactly = 1)  { _pathsServiceMock.getPath(PathType.WorkingDirectory) }
    }

    @DataProvider
    fun `exit code and number of callback invocations`() = arrayOf(
        arrayOf(0, 0),
        arrayOf(42, 1),
        arrayOf(-1, 1),
    )
    @Test(dataProvider = "exit code and number of callback invocations")
    fun `should setup command line to fail build when command line returns non-zero exit code and fail build on exit code option is enabled`(
        exitCode: Int, callbackInvocations: Int
    ) {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE) } returns "abc"
        every { _virtualContextMock.resolvePath(any()) } returns "abc"
        every { _pathsServiceMock.getPath(any()) } returns File("workingDirectory")
        every { _buildOptionsMock.failBuildOnExitCode } returns true
        val workflowContext = WorkflowContextStub(WorkflowStatus.Failed, CommandResultExitCode(exitCode))
            .let(::spyk)

        // act
        _instance.compose(workflowContext, mockk(), mockk())

        // assert
        verify(exactly = callbackInvocations) { _buildOptionsMock.failBuildOnExitCode }
        verify(exactly = callbackInvocations) {
            _loggerServiceMock.writeBuildProblem(
                "dotcover_cover_custom_process_exit_code$exitCode",
                BuildProblemData.TC_EXIT_CODE_TYPE,
                "Process exited with code $exitCode"
            )
        }
        verify(exactly = callbackInvocations) { workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED) }
    }
}