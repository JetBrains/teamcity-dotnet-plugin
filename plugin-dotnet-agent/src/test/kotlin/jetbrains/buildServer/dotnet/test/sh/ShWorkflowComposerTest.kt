/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.test.sh

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.sh.ShWorkflowComposer
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ShWorkflowComposerTest {
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _cannotExecute: CannotExecute
    @MockK private lateinit var _workflowContext: WorkflowContext
    private var _baseCommandLineSh = createBaseCommandLine(Path(File("abc1", "my.sH").path))
    private var _workflowSh = createWorkflow(_baseCommandLineSh)
    private var _baseCommandLineOther = createBaseCommandLine(Path(File("abc3", "my.dll").path))
    private var _workflowOther = createWorkflow(_baseCommandLineOther)

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
        every { _virtualContext.isVirtual } returns true
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
                arrayOf(OSType.WINDOWS, _workflowSh, Workflow(), true),
                arrayOf(OSType.WINDOWS, _workflowOther, _workflowOther, false),
                arrayOf(OSType.UNIX, _workflowOther, _workflowOther, false),
                arrayOf(OSType.MAC, _workflowOther, _workflowOther, false),
                arrayOf(
                        OSType.UNIX,
                        _workflowSh,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                _baseCommandLineSh,
                                                TargetType.Host,
                                                Path("sh"),
                                                Path(_workflowSh.commandLines.single().workingDirectory.path),
                                                listOf(
                                                        CommandLineArgument("-c"),
                                                        CommandLineArgument("\"v_${_workflowSh.commandLines.single().executableFile.path} ${_workflowSh.commandLines.single().arguments.joinToString(" ") { "v_" + it.value }}\"", CommandLineArgumentType.Target)),
                                                _workflowSh.commandLines.single().environmentVariables
                                        )
                                )
                        ),
                        false
                )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            osType: OSType,
            baseWorkflow: Workflow,
            expectedWorkflow: Workflow,
            hasWarning: Boolean) {
        // Given
        val composer = createInstance()
        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _virtualContext.targetOSType } returns osType
        if(hasWarning) {
            every { _cannotExecute.writeBuildProblemFor(any()) } returns Unit
        }

        // When
        val actualCommandLines = composer.compose(_workflowContext, Unit, baseWorkflow).commandLines.toList()

        // Then
        if (hasWarning) {
            verify { _cannotExecute.writeBuildProblemFor(any()) }
        }

        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance()=
            ShWorkflowComposer(
                ArgumentsServiceStub(),
                _virtualContext,
                _cannotExecute)

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