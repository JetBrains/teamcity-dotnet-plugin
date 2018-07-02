package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
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
    private lateinit var _dotnetCliToolInfo: DotnetCliToolInfo
    private lateinit var _dotnetToolResolver: DotnetToolResolver
    private lateinit var _pathsService: PathsService
    private lateinit var _agentRunningBuild: AgentRunningBuild

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _dotnetCliToolInfo = _ctx.mock(DotnetCliToolInfo::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _pathsService = _ctx.mock(PathsService::class.java)
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, Version(2, 1, 300), true),
                arrayOf(DotnetCommandType.Pack, Version(2, 1, 300), true),
                arrayOf(DotnetCommandType.Publish, Version(2, 1, 300), true),
                arrayOf(DotnetCommandType.Test, Version(2, 1, 300), true),
                arrayOf(DotnetCommandType.Run, Version(2, 1, 300), true),
                arrayOf(DotnetCommandType.MSBuild, Version(2, 1, 300), true))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldShutdownDotnetBuildServer(dotnetCommandType: DotnetCommandType, version: Version, expectedShutdown: Boolean) {
        // Given
        val executableFile = File("dotnet")
        val checkout = File("checkoutDir")
        val buildServerShutdownCommandline = CommandLine(
                TargetType.Tool,
                executableFile,
                checkout,
                BuildServerShutdownMonitor.shutdownArgs,
                emptyList())

        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                allowing<DotnetCliToolInfo>(_dotnetCliToolInfo).version
                will(returnValue(version))

                if (expectedShutdown) {
                    oneOf<DotnetToolResolver>(_dotnetToolResolver).executableFile
                    will(returnValue(executableFile))

                    oneOf<PathsService>(_pathsService).getPath(PathType.Checkout)
                    will(returnValue(checkout))

                    oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(buildServerShutdownCommandline)
                }
            }
        })

        val monitor = createInstance()

        // When
        monitor.register(dotnetCommandType)
        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(_agentRunningBuild, BuildFinishedStatus.FINISHED_SUCCESS))

        // Then
        _ctx.assertIsSatisfied()
    }

    private fun createInstance() =
            BuildServerShutdownMonitor(
                    _agentLifeCycleEventSources,
                    _commandLineExecutor,
                    _dotnetCliToolInfo,
                    _dotnetToolResolver,
                    _pathsService)
}