package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.nunit.*
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateParser
import jetbrains.buildServer.rx.observer
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Paths

class NUnitToolStateWorkflowComposerTest {
    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _nUnitConsoleRunnerPathProvider: NUnitConsoleRunnerPathProvider

    @MockK
    private lateinit var _nUnitToolStateParser: NUnitToolStateParser

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun `should call nunit console to check version and extensions`() {
        // arrange
        val expectedNUnitToolState = NUnitToolState("nunit-version", listOf("ext1", "ext2"))

        every { _nUnitConsoleRunnerPathProvider.consoleRunnerPath } returns Paths.get("nunit3-console.exe")
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns File("work-dir")
        every { _nUnitToolStateParser.parse(any(), any()) } returns expectedNUnitToolState

        val composer = NUnitToolStateWorkflowComposer(
            _pathsService,
            _nUnitConsoleRunnerPathProvider,
            _nUnitToolStateParser
        )

        // act
        var nUnitToolState: NUnitToolState? = null
        val commands = composer
            .compose(WorkflowContextStub(WorkflowStatus.Running), observer { nUnitToolState = it })
            .commandLines.toList()

        // assert
        assertNotNull(nUnitToolState)
        assertEquals(nUnitToolState, expectedNUnitToolState)

        assertEquals(commands.size, 1)
        assertEquals(
            commands[0].arguments, listOf(
                CommandLineArgument("--list-extensions"),
                CommandLineArgument("--teamcity")
            )
        )
    }
}