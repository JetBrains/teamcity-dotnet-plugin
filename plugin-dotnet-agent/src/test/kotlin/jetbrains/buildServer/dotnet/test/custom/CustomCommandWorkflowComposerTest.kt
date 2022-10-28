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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.BuildProblemTypes
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.custom.CustomCommandWorkflowComposer
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CustomCommandWorkflowComposerTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _targetService: TargetService
    @MockK private lateinit var _buildOptions: BuildOptions
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _workflowContext: WorkflowContext

    private val _workingDirectory = File("wd")
    private val _args = listOf(
        CommandLineArgument("arg1", CommandLineArgumentType.Custom),
        CommandLineArgument("arg2", CommandLineArgumentType.Custom)
    )

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
        mockkStatic(Observable<CommandResultEvent>::toExitCodes)

        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
        every { _workflowContext.status } returns WorkflowStatus.Running
        every { _workflowContext.toExitCodes() } returns mockk<Subject<Int>> {
            every { subscribe(any()) } returns mockk(relaxed = true)
        }
        every { _buildOptions.failBuildOnExitCode } returns false
        every {
            _parametersService.tryGetParameter(
                ParameterType.Runner,
                DotnetConstants.PARAM_COMMAND
            )
        } returns DotnetCommandType.Custom.id
        every {
            _parametersService.tryGetParameter(
                ParameterType.Runner,
                DotnetConstants.PARAM_ARGUMENTS
            )
        } returns "args"

        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _argumentsService.split("args") } returns sequenceOf("arg1", "arg2")
        every { _targetService.targets } returns emptySequence()
        every { _loggerService.writeBuildProblem(any(), any(), any()) } just Runs
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                sequenceOf("abc.exe"),
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
                )
            ),
            arrayOf(
                sequenceOf("abc.com"),
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
                )
            ),
            arrayOf(
                sequenceOf("abc.Cmd"),
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
                )
            ),
            arrayOf(
                sequenceOf("abc.baT"),
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
                )
            ),
            // without targets
            arrayOf(
                emptySequence<String>(),
                Workflow(
                    sequenceOf(
                        CommandLine(
                            null,
                            TargetType.Tool,
                            Path(""),
                            Path(_workingDirectory.path),
                            _args
                        )
                    )
                )
            )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
        targets: Sequence<String>,
        expectedWorkflow: Workflow,
    ) {
        // arrange
        val composer = createInstance()
        every { _targetService.targets } returns targets.map { CommandTarget(Path(it)) }

        // act
        val actualWorkflow = composer.compose(_workflowContext, Unit)

        // assert
        Assert.assertEquals(actualWorkflow.commandLines.toList(), expectedWorkflow.commandLines.toList())
    }

    @Test
    fun shouldNotComposeWhenItIsNotACommand() {
        // arrange
        val composer = createInstance()
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) } returns null

        // act
        val actualWorkflow = composer.compose(_workflowContext, Unit)

        // assert
        Assert.assertEquals(actualWorkflow.commandLines.toList(), emptyList<CommandLine>())
    }

    @DataProvider(name = "exitCodeAndFailureCondition")
    fun getExitCodeAndFailureConditionCombinationCases(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(0, true, false),
            arrayOf(0, false, false),
            arrayOf(1, true, true),
            arrayOf(1, false, false),
        )
    }

    @Test(dataProvider = "exitCodeAndFailureCondition")
    fun shouldRespectExitCodeAndFailureConditionCombination(
        exitCode: Int,
        failBuildOnNonZeroExitCode: Boolean,
        failBuild: Boolean
    ) {
        // arrange
        val composer = createInstance()
        every { _buildOptions.failBuildOnExitCode } returns failBuildOnNonZeroExitCode

        val exitCodesSubject = subjectOf<Int>()
        every { _workflowContext.toExitCodes() } returns exitCodesSubject
        every { _workflowContext.abort(any()) } just Runs

        // act
        val actualWorkflow = composer.compose(_workflowContext, Unit)
        val iterator = actualWorkflow.commandLines.iterator()
        iterator.next()
        exitCodesSubject.onNext(exitCode)

        // assert
        verify(exactly = (if (failBuild) 1 else 0)) {
            _loggerService.writeBuildProblem(
                "dotnet_custom_exit_code$exitCode",
                BuildProblemTypes.TC_EXIT_CODE_TYPE,
                "Process exited with code $exitCode")
            _workflowContext.abort(BuildFinishedStatus.FINISHED_FAILED)
        }
    }

    private fun createInstance(): SimpleWorkflowComposer {
        return CustomCommandWorkflowComposer(
            _parametersService,
            _argumentsService,
            _pathsService,
            _targetService,
            _buildOptions,
            _loggerService,
        )
    }
}