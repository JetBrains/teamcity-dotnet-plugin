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

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.EnvironmentVariablesImpl.Companion.UseSharedCompilationEnvVarName
import jetbrains.buildServer.rx.subjectOf
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class BuildServerShutdownMonitorTest {
    private lateinit var _ctx: Mockery
    private lateinit var eventSources: EventSources
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _dotnetToolResolver: DotnetToolResolver
    private lateinit var _agentRunningBuild: AgentRunningBuild
    private lateinit var _parametersService: ParametersService
    private lateinit var _environmentVariables: EnvironmentVariables
    private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        eventSources = _ctx.mock(EventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
        _environmentVariables = _ctx.mock(EnvironmentVariables::class.java)
        _virtualContext = _ctx.mock(VirtualContext::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), "true", true),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), "True", true),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), "abc", false),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), "false", false),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), "FaLse", false),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.Build, Version(2, 1, 301), null, true),
                arrayOf(DotnetCommandType.Pack, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.Pack, Version(2, 1, 301), null, true),
                arrayOf(DotnetCommandType.Publish, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.Test, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.Test, Version(1, 0, 0), null, true),
                arrayOf(DotnetCommandType.Test, Version(1, 0, 0), null, false),
                arrayOf(DotnetCommandType.Run, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.MSBuild, Version(2, 1, 300), null, true),
                arrayOf(DotnetCommandType.NuGetPush, Version(2, 1, 300), null, false),
                arrayOf(DotnetCommandType.NuGetDelete, Version(2, 1, 300), null, false),
                arrayOf(DotnetCommandType.Custom, Version(2, 1, 300), null, false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldShutdownDotnetBuildServer(dotnetCommandType: DotnetCommandType, toolVersion: Version, useSharedCompilationParam: String?, expectedShutdown: Boolean) {
        // Given
        val executableFile = Path("dotnet")
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, toolVersion)

        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(false))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue(useSharedCompilationParam))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(dotnetCommandType))

                allowing<DotnetToolResolver>(_dotnetToolResolver).executable
                will(returnValue(ToolPath(executableFile)))

                if (expectedShutdown) {
                    if (toolVersion > Version.LastVersionWithoutSharedCompilation ) {
                        val envVars = sequenceOf(CommandLineEnvironmentVariable("var1", "val1"), CommandLineEnvironmentVariable("var2", "val2"))
                        allowing<EnvironmentVariables>(_environmentVariables).getVariables(toolVersion)
                        will(returnValue(envVars))

                        val buildServerShutdownCommandline = CommandLine(
                                null,
                                TargetType.Tool,
                                executableFile,
                                Path("wd"),
                                BuildServerShutdownMonitor.shutdownArgs,
                                envVars.toList())

                        oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(buildServerShutdownCommandline)
                    }
                }
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)
        buildFinishedSource.onNext(EventSources.BuildFinished(BuildFinishedStatus.FINISHED_SUCCESS))

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldNotGetParameterOnBuildFinishedEvent() {
        // Given
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()
        _ctx.checking(object : Expectations() {
            init {
                never<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        createInstance()

        // When
        buildFinishedSource.onNext(EventSources.BuildFinished(BuildFinishedStatus.FINISHED_SUCCESS))

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldShouldRegisterCommandForShotdown() {
        // Given
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, Version(2, 1, 300))
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(false))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue(null))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(DotnetCommandType.Test))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(monitor.count, 1)
    }

    @Test
    fun shouldShouldNotRegisterCommandForShotdownWhenVirtualContext() {
        // Given
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, Version(2, 1, 300))
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(true))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue(null))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(DotnetCommandType.Test))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(monitor.count, 0)
    }

    @Test
    fun shouldShouldNotRegisterCommandForShotdownWhenUseSharedCompilationEnvVarIsFalse() {
        // Given
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, Version(2, 1, 300))
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(false))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue("false"))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(DotnetCommandType.Test))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(monitor.count, 0)
    }

    @Test
    fun shouldShouldNotRegisterCommandForShotdownWhenToolIsNotSupportingShutdown() {
        // Given
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, Version(2, 1, 105))
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(false))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue(null))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(DotnetCommandType.Test))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(monitor.count, 0)
    }

    @Test
    fun shouldShouldNotRegisterCommandForShotdownWhenCommandDoesNotStartCompilationService() {
        // Given
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(Path("wd")), command, Version(2, 1, 300))
        val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

        _ctx.checking(object : Expectations() {
            init {
                oneOf<VirtualContext>(_virtualContext).isVirtual
                will(returnValue(false))

                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)
                will(returnValue(null))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(DotnetCommandType.NuGetPush))

                oneOf<EventSources>(eventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(context)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(monitor.count, 0)
    }

    private fun createInstance() =
            BuildServerShutdownMonitor(
                    eventSources,
                    _commandLineExecutor,
                    _dotnetToolResolver,
                    _parametersService,
                    _environmentVariables,
                    _virtualContext)
}