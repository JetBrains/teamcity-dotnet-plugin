package jetbrains.buildServer.dotnet.test.cmd

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowComposer
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.cmd.CmdWorkflowComposer
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CmdWorkflowComposerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _environment: Environment
    private lateinit var _workflowContext: WorkflowContext
    private var _workflowCmd = createWorkflow(File("abc1", "my.cmd"))
    private var _workflowBat = createWorkflow(File("abc2", "my.bat"))
    private var _workflowOther = createWorkflow(File("abc3", "my.exe"))

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _environment = _ctx.mock(Environment::class.java)
        _workflowContext = _ctx.mock(WorkflowContext::class.java)
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
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.MAC, "cmd", _workflowCmd, _workflowCmd),
                arrayOf(OSType.UNIX, "cmd", _workflowBat, _workflowBat),
                arrayOf(OSType.UNIX, "cmd", _workflowOther, _workflowOther),
                arrayOf(OSType.WINDOWS, File("win", "cmd.exe").path, _workflowOther, _workflowOther),
                arrayOf(
                        OSType.WINDOWS,
                        File("win", "cmd.exe").path,
                        _workflowCmd,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                TargetType.Host,
                                                File("win", "cmd.exe"),
                                                _workflowCmd.commandLines.single().workingDirectory,
                                                listOf(
                                                        CommandLineArgument("/D"),
                                                        CommandLineArgument("/C"),
                                                        CommandLineArgument("\"${_workflowCmd.commandLines.single().executableFile.absolutePath} ${_workflowCmd.commandLines.single().arguments.joinToString(" ") { it.value }}\"")),
                                                _workflowCmd.commandLines.single().environmentVariables
                                        )
                                )
                        )
                ),
                arrayOf(
                        OSType.WINDOWS,
                        File("win", "cmd.exe").path,
                        _workflowBat,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                TargetType.Host,
                                                File("win", "cmd.exe"),
                                                _workflowBat.commandLines.single().workingDirectory,
                                                listOf(
                                                        CommandLineArgument("/D"),
                                                        CommandLineArgument("/C"),
                                                        CommandLineArgument("\"${_workflowBat.commandLines.single().executableFile.absolutePath} ${_workflowBat.commandLines.single().arguments.joinToString(" ") { it.value }}\"")),
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

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<Environment>(_environment).OS
                will(returnValue(osType))

                allowing<Environment>(_environment).tryGetVariable(CmdWorkflowComposer.ComSpecEnvVarName)
                will(returnValue(cmdFile))
            }
        })

        val actualCommandLines = composer.compose(_workflowContext, baseWorkflow).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(): WorkflowComposer {
        return CmdWorkflowComposer(
                ArgumentsServiceStub(),
                _environment)
    }

    companion object {
        private fun createWorkflow(executableFile: File): Workflow {
            val workingDirectory = File("wd")
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