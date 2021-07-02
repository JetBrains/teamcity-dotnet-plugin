package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.script.CSharpScriptWorkflowComposer
import jetbrains.buildServer.script.CommandLineFactory
import jetbrains.buildServer.script.ScriptConstants
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class CSharpScriptWorkflowComposerTest {
    @MockK private lateinit var _buildInfo: BuildInfo
    @MockK private lateinit var _commandLineFactory: CommandLineFactory
    @MockK private lateinit var _context: WorkflowContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldComposeWokflow() {
        // Given
        val composer = createInstance()
        every { _buildInfo.runType } returns ScriptConstants.RUNNER_TYPE
        val commandLine = CommandLine(null, TargetType.Tool, Path("Abc"), Path("Wd"))
        every { _commandLineFactory.create() } returns commandLine

        // When
        val actualWorkflow = composer.compose(_context, Unit, Workflow())

        // Then
        Assert.assertEquals(actualWorkflow.commandLines.single(), commandLine)
    }

    @Test
    fun shouldNotComposeWokflowWhenUnsupportedRunnerType() {
        // Given
        val composer = createInstance()

        // When
        every { _buildInfo.runType } returns "Abc"
        val actualWorkflow = composer.compose(_context, Unit, Workflow())

        // Then
        Assert.assertEquals(actualWorkflow.commandLines.count(), 0)
    }

    private fun createInstance() = CSharpScriptWorkflowComposer(_buildInfo, _commandLineFactory)
}