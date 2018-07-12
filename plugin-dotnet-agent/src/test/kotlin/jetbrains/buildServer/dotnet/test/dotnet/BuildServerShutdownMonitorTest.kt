package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
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

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _agentLifeCycleEventSources = _ctx.mock(AgentLifeCycleEventSources::class.java)
        _commandLineExecutor = _ctx.mock(CommandLineExecutor::class.java)
        _dotnetToolResolver = _ctx.mock(DotnetToolResolver::class.java)
        _agentRunningBuild = _ctx.mock(AgentRunningBuild::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.Build, sequenceOf(Version(1, 0, 0), Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.Build, emptySequence<Version>(), false),
                arrayOf(DotnetCommandType.Pack, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.Publish, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(2, 1, 300), Version(1, 0, 0)), true),
                arrayOf(DotnetCommandType.Test, sequenceOf(Version(1, 1, 0), Version(1, 0, 0)), false),
                arrayOf(DotnetCommandType.Test, emptySequence<Version>(), false),
                arrayOf(DotnetCommandType.Run, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.MSBuild, sequenceOf(Version(2, 1, 300)), true),
                arrayOf(DotnetCommandType.NuGetPush, sequenceOf(Version(2, 1, 300)), false),
                arrayOf(DotnetCommandType.NuGetDelete, sequenceOf(Version(2, 1, 300)), false),
                arrayOf(DotnetCommandType.Custom, sequenceOf(Version(2, 1, 300)), false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldShutdownDotnetBuildServer(dotnetCommandType: DotnetCommandType, versions: Sequence<Version>, expectedShutdown: Boolean) {
        // Given
        val executableFile = File("dotnet")
        val taregtPath = File("checkoutDir")
        val buildServerShutdownCommandline = CommandLine(
                TargetType.Tool,
                executableFile,
                taregtPath,
                BuildServerShutdownMonitor.shutdownArgs,
                emptyList())

        val command = _ctx.mock(DotnetCommand::class.java)
        val context = DotnetBuildContext(command, null, versions.map { DotnetSdk(CommandLineArgument("target"), taregtPath, it) }.toSet())

        val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()
        _ctx.checking(object : Expectations() {
            init {
                oneOf<AgentLifeCycleEventSources>(_agentLifeCycleEventSources).buildFinishedSource
                will(returnValue(buildFinishedSource))

                oneOf<DotnetCommand>(command).commandType
                will(returnValue(dotnetCommandType))

                if (expectedShutdown) {
                    oneOf<DotnetToolResolver>(_dotnetToolResolver).executableFile
                    will(returnValue(executableFile))

                    oneOf<CommandLineExecutor>(_commandLineExecutor).tryExecute(buildServerShutdownCommandline)
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

    private fun createInstance() =
            BuildServerShutdownMonitor(
                    _agentLifeCycleEventSources,
                    _commandLineExecutor,
                    _dotnetToolResolver)
}