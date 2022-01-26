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

package jetbrains.buildServer.dotnet.test.visualStudio

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CommandTarget
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.dotnet.test.agent.runner.WorkflowContextStub
import jetbrains.buildServer.visualStudio.ToolResolver
import jetbrains.buildServer.visualStudio.VisualStudioWorkflowComposer
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioWorkflowComposerTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _targetService: TargetService
    private lateinit var _toolResolver: ToolResolver
    private lateinit var _loggerService: LoggerService
    private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _targetService = _ctx.mock(TargetService::class.java)
        _toolResolver = _ctx.mock(ToolResolver::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
        _virtualContext = _ctx.mock(VirtualContext::class.java)
    }

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "build",
                                DotnetConstants.PARAM_CONFIG to "Debug",
                                DotnetConstants.PARAM_PLATFORM to "x86",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2"),
                        sequenceOf(CommandTarget(Path("my1.sln")), CommandTarget(Path("my2.sln"))),
                        listOf(
                                CommandLine(
                                        null,
                                        TargetType.Tool,
                                        Path("v_tool"),
                                        Path("wd"),
                                        listOf(
                                                CommandLineArgument("my1.sln", CommandLineArgumentType.Target),
                                                CommandLineArgument("/build", CommandLineArgumentType.Mandatory),
                                                CommandLineArgument("\"Debug|x86\""),
                                                CommandLineArgument("arg1", CommandLineArgumentType.Custom),
                                                CommandLineArgument("arg2", CommandLineArgumentType.Custom)),
                                        emptyList(),
                                        DotnetCommandType.VisualStudio.id),
                                CommandLine(
                                        null,
                                        TargetType.Tool,
                                        Path("v_tool"),
                                        Path("wd"),
                                        listOf(
                                                CommandLineArgument("my2.sln", CommandLineArgumentType.Target),
                                                CommandLineArgument("/build", CommandLineArgumentType.Mandatory),
                                                CommandLineArgument("\"Debug|x86\""),
                                                CommandLineArgument("arg1", CommandLineArgumentType.Custom),
                                                CommandLineArgument("arg2", CommandLineArgumentType.Custom)),
                                        emptyList(),
                                        DotnetCommandType.VisualStudio.id))),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(Path("my1.csproj"))),
                        listOf(
                                CommandLine(
                                        null,
                                        TargetType.Tool,
                                        Path("v_tool"),
                                        Path("wd"),
                                        listOf(
                                                CommandLineArgument("my1.csproj", CommandLineArgumentType.Target),
                                                CommandLineArgument("/rebuild", CommandLineArgumentType.Mandatory),
                                                CommandLineArgument("release"),
                                                CommandLineArgument("arg1", CommandLineArgumentType.Custom)),
                                        emptyList(),
                                        DotnetCommandType.VisualStudio.id))),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_COMMAND to "abc",
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(Path("my1.csproj"))),
                        emptyList<CommandLine>()),
                arrayOf(
                        mapOf(
                                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "rebuild",
                                DotnetConstants.PARAM_CONFIG to "release",
                                DotnetConstants.PARAM_ARGUMENTS to "arg1"),
                        sequenceOf(CommandTarget(Path("my1.csproj"))),
                        emptyList<CommandLine>()))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            parameters: Map<String, String>,
            targets: Sequence<CommandTarget>,
            expectedCommandLines: List<CommandLine>) {
        // Given

        val workingDirectory = File("wd")
        val composer = createInstance(parameters)

        // When
        _ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<ToolResolver>(_toolResolver).executableFile
                will(returnValue(File("tool")))

                allowing<TargetService>(_targetService).targets
                will(returnValue(targets))

                allowing<VirtualContext>(_virtualContext).resolvePath(File("tool").canonicalPath)
                will(returnValue("v_tool"))
            }
        })

        val actualCommandLines = composer.compose(WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(0)), Unit).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
    }

    @DataProvider(name = "abortCases")
    fun getAbortCases(): Array<Array<Int>> {
        return arrayOf(arrayOf(1), arrayOf(-1), arrayOf(10000))
    }

    @Test(dataProvider = "abortCases")
    fun shouldAbortWhenStepFailed(exitCode: Int) {
        // Given
        val parameters = mapOf(
                DotnetConstants.PARAM_COMMAND to DotnetCommandType.VisualStudio.id,
                DotnetConstants.PARAM_VISUAL_STUDIO_ACTION to "build",
                DotnetConstants.PARAM_CONFIG to "Debug",
                DotnetConstants.PARAM_PLATFORM to "x86",
                DotnetConstants.PARAM_ARGUMENTS to "arg1 arg2")

        val targets = sequenceOf(CommandTarget(Path("my1.sln")), CommandTarget(Path("my2.sln")))
        val expectedCommandLines = listOf(
                CommandLine(
                        null,
                        TargetType.Tool,
                        Path("v_tool"),
                        Path("wDir"),
                        listOf(
                                CommandLineArgument("my1.sln", CommandLineArgumentType.Target),
                                CommandLineArgument("/build", CommandLineArgumentType.Mandatory),
                                CommandLineArgument("\"Debug|x86\""),
                                CommandLineArgument("arg1", CommandLineArgumentType.Custom),
                                CommandLineArgument("arg2", CommandLineArgumentType.Custom)),
                        emptyList(),
                        DotnetCommandType.VisualStudio.id))

        val workingDirectory = File("wDir")
        val composer = createInstance(parameters)

        // When
        _ctx.checking(object : Expectations() {
            init {
                allowing<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<ToolResolver>(_toolResolver).executableFile
                will(returnValue(File("tool")))

                allowing<TargetService>(_targetService).targets
                will(returnValue(targets))

                oneOf<VirtualContext>(_virtualContext).resolvePath(File("tool").canonicalPath)
                will(returnValue("v_tool"))

                oneOf<LoggerService>(_loggerService).writeBuildProblem("visual_studio_exit_code$exitCode", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code $exitCode")
            }
        })

        var context = WorkflowContextStub(WorkflowStatus.Running, CommandResultExitCode(exitCode));
        val actualCommandLines = composer.compose(context, Unit).commandLines.toList()

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualCommandLines, expectedCommandLines)
        Assert.assertEquals(context.status, WorkflowStatus.Failed)
    }

    private fun createInstance(parameters: Map<String, String>): SimpleWorkflowComposer {
        return VisualStudioWorkflowComposer(
                ParametersServiceStub(parameters),
                ArgumentsServiceStub(),
                _pathService,
                _loggerService,
                _targetService,
                _toolResolver,
                _virtualContext)
    }
}