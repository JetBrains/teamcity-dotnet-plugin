/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.test.cmd

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.SimpleWorkflowComposer
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowComposer
import jetbrains.buildServer.agent.runner.WorkflowContext
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
    private var _baseCommandLineCmd = createBaseCommandLine(Path(File("abc1", "my.cmd").path))
    private var _workflowCmd = createWorkflow(_baseCommandLineCmd)
    private var _baseCommandLineBat = createBaseCommandLine(Path(File("abc2", "my.bat").path))
    private var _workflowBat = createWorkflow(_baseCommandLineBat)
    private var _baseCommandLineOther = createBaseCommandLine(Path(File("abc3", "my.exe").path))
    private var _workflowOther = createWorkflow(_baseCommandLineOther)
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
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.MAC, _workflowCmd, _workflowCmd),
                arrayOf(OSType.UNIX, _workflowBat, _workflowBat),
                arrayOf(OSType.UNIX, _workflowOther, _workflowOther),
                arrayOf(OSType.WINDOWS, _workflowOther, _workflowOther),
                arrayOf(
                        OSType.WINDOWS,
                        _workflowCmd,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                _baseCommandLineCmd,
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
                        _workflowBat,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                _baseCommandLineBat,
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
            baseWorkflow: Workflow,
            expectedWorkflow: Workflow) {
        // Given
        val composer = createInstance()
        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _virtualContext.targetOSType } returns osType

        // When
        val actualCommandLines = composer.compose(_workflowContext, Unit, baseWorkflow).commandLines.toList()

        // Then
        // verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(): SimpleWorkflowComposer {
        return CmdWorkflowComposer(
                ArgumentsServiceStub(),
                _environment,
                _virtualContext)
    }

    companion object {
        private fun createWorkflow(baseCommandLine: CommandLine) =
            Workflow(sequenceOf(baseCommandLine))

        private fun createBaseCommandLine(executableFile: Path): CommandLine {
            val workingDirectory = Path("wd")
            val args = listOf(CommandLineArgument("arg1"))
            val envVars = listOf(CommandLineEnvironmentVariable("var1", "val1"))
            val commandLine = CommandLine(
                    null,
                    TargetType.Tool,
                    executableFile,
                    workingDirectory,
                    args,
                    envVars)
            return commandLine
        }
    }
}