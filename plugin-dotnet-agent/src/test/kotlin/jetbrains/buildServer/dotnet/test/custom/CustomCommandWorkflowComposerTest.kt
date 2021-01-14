/*
 * Copyright 2000-2021 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.custom.CustomCommandWorkflowComposer
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.Observer
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class CustomCommandWorkflowComposerTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _targetService: TargetService
    @MockK private lateinit var _dotnetToolResolver: DotnetToolResolver
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _dotnetStateWorkflowComposer: ToolStateWorkflowComposer
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _workflowContext: WorkflowContext
    @MockK private lateinit var _environmentVariables: EnvironmentVariables

    private val _workingDirectory = File("wd")
    private val _args = listOf(CommandLineArgument("arg1", CommandLineArgumentType.Custom), CommandLineArgument("arg2", CommandLineArgumentType.Custom))
    private val _dotnetEnvVars = listOf(CommandLineEnvironmentVariable("env222", "val222"))

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        every { _pathsService.getPath(PathType.WorkingDirectory) } returns _workingDirectory
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        sequenceOf("abc.exe"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.exe"),
                                                Path(_workingDirectory.path),
                                                _args,
                                                emptyList<CommandLineEnvironmentVariable>()
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf("abc.com"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.com"),
                                                Path(_workingDirectory.path),
                                                _args,
                                                emptyList<CommandLineEnvironmentVariable>()
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf("abc.Cmd"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.Cmd"),
                                                Path(_workingDirectory.path),
                                                _args,
                                                emptyList<CommandLineEnvironmentVariable>()
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf("abc.baT"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("abc.baT"),
                                                Path(_workingDirectory.path),
                                                _args,
                                                emptyList<CommandLineEnvironmentVariable>()
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf("abc"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                listOf(CommandLineArgument("abc", CommandLineArgumentType.Target)) + _args,
                                                _dotnetEnvVars,
                                                "",
                                                listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("${Version(1, 2, 3)} ", Color.Header))
                                        )
                                )
                        )
                ),
                arrayOf(
                        sequenceOf("abc.Dll"),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                listOf(CommandLineArgument("abc.Dll", CommandLineArgumentType.Target)) + _args,
                                                _dotnetEnvVars,
                                                "",
                                                listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("${Version(1, 2, 3)} ", Color.Header))
                                        )
                                )
                        )
                ),
                // without targets
                arrayOf(
                        emptySequence<String>(),
                        true,
                        Workflow(
                                sequenceOf(
                                        CommandLine(
                                                null,
                                                TargetType.Tool,
                                                Path("vdotnet"),
                                                Path(_workingDirectory.path),
                                                _args,
                                                _dotnetEnvVars,
                                                "",
                                                listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("${Version(1, 2, 3)} ", Color.Header))
                                        )
                                )
                        )
                )
        )
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            targets: Sequence<String>,
            isVirtual: Boolean,
            expectedWorkflow: Workflow) {
        // Given
        val composer = createInstance()
        val pathObserver = mockk<Observer<Path>>()
        every { pathObserver.onNext(any()) } returns Unit
        every { _workflowContext.status } returns WorkflowStatus.Running
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND) } returns DotnetCommandType.Custom.id
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS) } returns "args"
        every { _argumentsService.split("args") } returns sequenceOf("arg1", "arg2")
        every { _targetService.targets } returns targets.map { CommandTarget(Path(it)) }
        val defaultDotnetExecutableFile = ToolPath(Path("dotnet"))
        every { _dotnetToolResolver.executable } returns defaultDotnetExecutableFile
        every { _virtualContext.isVirtual } returns isVirtual
        every { _dotnetStateWorkflowComposer.compose(_workflowContext, match { state -> defaultDotnetExecutableFile.equals(state.executable)}) } answers {
            val state = arg<ToolState>(1)
            state.virtualPathObserver.onNext(Path("vdotnet"))
            state.versionObserver.onNext(Version(1, 2, 3))
            Workflow()
        }
        every { _environmentVariables.getVariables(Version(1, 2, 3)) } returns _dotnetEnvVars.asSequence()

        // When
        val actualCommandLines = composer.compose(_workflowContext, Unit).commandLines.toList()

        // Then
        // verify { _virtualContext.resolvePath(any()) }
        Assert.assertEquals(actualCommandLines, expectedWorkflow.commandLines.toList())
    }

    private fun createInstance(): SimpleWorkflowComposer {
        return CustomCommandWorkflowComposer(
                _parametersService,
                _argumentsService,
                _pathsService,
                _loggerService,
                _targetService,
                _dotnetToolResolver,
                _fileSystemService,
                _dotnetStateWorkflowComposer,
                _virtualContext,
                _environmentVariables)
    }
}