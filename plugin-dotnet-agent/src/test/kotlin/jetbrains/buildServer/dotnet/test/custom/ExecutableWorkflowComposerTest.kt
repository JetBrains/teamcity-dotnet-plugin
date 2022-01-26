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

package jetbrains.buildServer.dotnet.test.custom

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.custom.ExecutableWorkflowComposer
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ExecutableWorkflowComposerTest {
    @MockK private lateinit var _dotnetToolResolver: DotnetToolResolver
    @MockK private lateinit var _dotnetStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _workflowContext: WorkflowContext
    @MockK private lateinit var _environmentVariables: EnvironmentVariables
    @MockK private lateinit var _cannotExecute: CannotExecute

    private val _workingDirectory = File("wd")
    private val _args = listOf(CommandLineArgument("arg1", CommandLineArgumentType.Custom), CommandLineArgument("arg2", CommandLineArgumentType.Custom))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc.exe"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.exe"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.UNIX,
                        sequenceOf("abc.exe"),
                        true,
                        Workflow(),
                        true
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc.com"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.com"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc.Cmd"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.Cmd"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.UNIX,
                        sequenceOf("abc.Cmd"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.Cmd"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc.baT"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.baT"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf("abc.Dll"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                listOf(CommandLineArgument("abc.Dll", CommandLineArgumentType.Target)) + _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.UNIX,
                        sequenceOf("abc.Dll"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                listOf(CommandLineArgument("abc.Dll", CommandLineArgumentType.Target)) + _args
                                        )
                                )
                        ),
                        false
                ),
                arrayOf(
                        OSType.WINDOWS,
                        sequenceOf(""),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                _args
                                        )
                                )
                        ),
                        false
                ),
                // without targets
                arrayOf(
                        OSType.WINDOWS,
                        emptySequence<String>(),
                        true,
                        Workflow(),
                        false
                )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            os: OSType,
            targets: Sequence<String>,
            isVirtual: Boolean,
            expectedWorkflow: Workflow,
            cannotExecute: Boolean) {
        // Given
        val composer = createInstance()
        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _workflowContext.status } returns WorkflowStatus.Running
        val defaultDotnetExecutableFile = ToolPath(Path("dotnet"))
        every { _dotnetToolResolver.executable } returns defaultDotnetExecutableFile
        every { _virtualContext.isVirtual } returns isVirtual
        every { _virtualContext.targetOSType } returns os
        every { _cannotExecute.writeBuildProblemFor(any()) } returns Unit
        every { _dotnetStateWorkflowComposer.compose(_workflowContext, match { state -> defaultDotnetExecutableFile.equals(state.executable)}) } answers {
            val state = arg<ToolState>(1)
            state.virtualPathObserver.onNext(Path("vdotnet"))
            Workflow()
        }
        val commandLines = targets
                .map {
                    CommandLine(
                    null,
                    TargetType.Tool,
                    Path(it),
                    Path(_workingDirectory.path),
                    _args)
                }

        // When
        val actualCommandLines = composer.compose(_workflowContext, Unit, Workflow(commandLines)).commandLines.toList()

        // Then
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
        if(cannotExecute) {
            verify { _cannotExecute.writeBuildProblemFor(Path(targets.first())) }
        }
    }

    private fun createInstance(): ExecutableWorkflowComposer {
        return ExecutableWorkflowComposer(
                _dotnetToolResolver,
                _dotnetStateWorkflowComposer,
                _virtualContext,
                _environmentVariables,
                _cannotExecute)
    }
}