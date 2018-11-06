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

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), "true", true),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), "True", true),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), "abc", false),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), "false", false),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), "FaLse", false),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(1, 0, 0), Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(1, 0, 0), Version(2, 1, 300), Version(2, 1, 301)), null, true),
                arrayOf(DotnetCommandType.Build, emptySequence<Version>(), null, false),
                arrayOf(DotnetCommandType.Pack, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.Pack, sequenceOf(Version(2, 1, 300), Version(2, 1, 301)), null, true),
                arrayOf(DotnetCommandType.Publish, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(2, 1, 300), Version(1, 0, 0)), null, true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(1, 1, 0), Version(1, 0, 0)), null, false),
                arrayOf(DotnetCommandType.Test, emptySequence<Version>(), null, false),
                arrayOf(DotnetCommandType.Run, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.MSBuild, sequenceOf(Version(2, 1, 300)), null, true),
                arrayOf(DotnetCommandType.NuGetPush, sequenceOf(Version(2, 1, 300)), null, false),
                arrayOf(DotnetCommandType.NuGetDelete, sequenceOf(Version(2, 1, 300)), null, false),
                arrayOf(DotnetCommandType.Custom, sequenceOf(Version(2, 1, 300)), null, false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldShutdownDotnetBuildServer(dotnetCommandType: DotnetCommandType, versions: Sequence<Version>, useSharedCompilationParam: String?, expectedShutdown: Boolean) {
        // Given
        val executableFile = File("dotnet")
        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(command, null, versions.map { DotnetSdk(File("wd$it"), it) }.toSet())

        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                allowing<ParametersService>(_parametersService).tryGetParameter(ParameterType.Environment, BuildServerShutdownMonitor.UseSharedCompilationEnvVarName)
                will(returnValue(useSharedCompilationParam))

                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<DotnetCommand>(command).commandType
                will(returnValue(dotnetCommandType))

                allowing<DotnetToolResolver>(_dotnetToolResolver).executableFile
                will(returnValue(executableFile))

                if (expectedShutdown) {
                    for(version in versions.filter { it > Version.LastVersionWithoutSharedCompilation }) {
                        val path = File("wd$version")
                        val buildServerShutdownCommandline = CommandLine(
                                TargetType.Tool,
                                executableFile,
                                path,
                                BuildServerShutdownMonitor.shutdownArgs,
                                emptyList())

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

        @Suppress("UNUSED_VARIABLE") val monitor = createInstance()

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
                    _parametersService)
}