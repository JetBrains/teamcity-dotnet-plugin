package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.subjectOf
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class BuildServerShutdownMonitorTest {
    private lateinit var _ctx: Mockery
    private lateinit var _agentLifeCycleEventSources: AgentLifeCycleEventSources
    private lateinit var _commandLineExecutor: CommandLineExecutor
    private lateinit var _dotnetToolResolver: DotnetToolResolver
    private lateinit var _agentRunningBuild: AgentRunningBuild
    private lateinit var _parametersService: ParametersService
    private lateinit var _environmentVariables: EnvironmentVariables

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
        _environmentVariables = _ctx.mock(EnvironmentVariables::class.java)
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
        val executableFile = File("dotnet")
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(ToolPath(File("wd")), command, toolVersion)

        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, BuildServerShutdownMonitor.UseSharedCompilationEnvVarName)
                will(returnValue(useSharedCompilationParam))

                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
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
                                TargetType.Tool,
                                executableFile,
                                File("wd"),
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
        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // Then
        _ctx.assertIsSatisfied()
    }

    @Test
    fun shouldNotGetParameterOnBuildFinishedEvent() {
        // Given
        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                never<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, BuildServerShutdownMonitor.UseSharedCompilationEnvVarName)

                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))
            }
        })

        createInstance()

        // When
        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // Then
        _ctx.assertIsSatisfied()
    }

    private fun createInstance() =
            BuildServerShutdownMonitor(
                    _agentLifeCycleEventSources,
                    _commandLineExecutor,
                    _dotnetToolResolver,
                    _parametersService,
                    _environmentVariables)
}