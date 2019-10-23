package jetbrains.buildServer.dotnet.test.cmd

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.cmd.CmdWorkflowComposer
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CmdWorkflowComposerTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _workflowContext: WorkflowContext
    private var _workflowCmd = createWorkflow(Path(File("abc1", "my.cmd").path))
    private var _workflowBat = createWorkflow(Path(File("abc2", "my.bat").path))
    private var _workflowOther = createWorkflow(Path(File("abc3", "my.exe").path))
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @Test
    fun shouldBeProfilerOfCodeCoverage() {
        // Given
        val composer = createInstance()

        // When

        // Then
        Assert.assertEquals(composer.target, TargetType.Host)
    }


    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(OSType.MAC, "cmd" as String?, _workflowCmd, _workflowCmd),
                arrayOf(OSType.UNIX, "cmd" as String?, _workflowBat, _workflowBat),
                arrayOf(OSType.UNIX, "cmd" as String?, _workflowOther, _workflowOther),
                arrayOf(OSType.WINDOWS, File("win", "cmd.exe").path, _workflowOther, _workflowOther),
                arrayOf(
                        OSType.WINDOWS,
                        null,
                        _workflowCmd,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                TargetType.Host,
                                                Path("cmd.exe"),
                                                Path(_workflowBat.commandLines.single().workingDirectory.path),
                                                listOf(
                                                        CommandLineArgument("/D"),
                                                        CommandLineArgument("/C"),
                                                        CommandLineArgument("\"v_${_workflowCmd.commandLines.single().executableFile.path} ${_workflowCmd.commandLines.single().arguments.joinToString(" ") { "v_" + it.value }}\"", CommandLineArgumentType.Target)),
                                                _workflowCmd.commandLines.single().environmentVariables
                                        )
                                )
                        )
                ),
                arrayOf(
                        OSType.WINDOWS,
                        "resolved_cmd" as String?,
                        _workflowBat,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                TargetType.Host,
                                                Path("cmd.exe"),
                                                Path(_workflowBat.commandLines.single().workingDirectory.path),
                                                listOf(
                                                        CommandLineArgument("/D"),
                                                        CommandLineArgument("/C"),
                                                        CommandLineArgument("\"v_${_workflowBat.commandLines.single().executableFile.path} ${_workflowBat.commandLines.single().arguments.joinToString(" ") { "v_" + it.value }}\"", CommandLineArgumentType.Target)),
                                                _workflowBat.commandLines.single().environmentVariables
                                        )
                                )
                        )
                )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            osType: OSType,
            cmdFile: String?,
            baseWorkflow: Workflow,
            expectedWorkflow: Workflow) {
        // Given
        val composer = createInstance()
        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _virtualContext.targetOSType } returns osType

        // When
        val actualCommandLines = composer.compose(_workflowContext, baseWorkflow).commandLines.toList()

        // Then
        // verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(): WorkflowComposer {
        return CmdWorkflowComposer(
                ArgumentsServiceStub(),
                _environment,
                _virtualContext)
    }

    companion object {
        private fun createWorkflow(executableFile: Path): Workflow {
            val workingDirectory = Path("wd")
            val args = listOf(CommandLineArgument("arg1"))
            val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
            val commandLine = CommandLine(
                    TargetType.Tool,
                    executableFile,
                    workingDirectory,
                    args,
                    envVars)

            return Workflow(sequenceOf(commandLine))
        }
    }
}