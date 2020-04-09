package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.BuildRunnerSettings
import jetbrains.buildServer.agent.EventSources
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NugetEnvironmentTest {
    @MockK private lateinit var _buildStepContext: BuildStepContext
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _runnerContext: BuildRunnerContext
    @MockK private lateinit var _build: AgentRunningBuild
    @MockK private lateinit var _sources: EventSources
    @MockK private lateinit var _stepStartedSource: Observable<EventSources.Event>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)

        every { _buildStepContext.isAvailable } returns true
        every { _buildStepContext.runnerContext } returns _runnerContext
        every { _runnerContext.build } returns _build
        every { _loggerService.writeWarning(any()) } returns Unit
        every { _sources.stepStartedSource } returns _stepStartedSource
        every { _stepStartedSource.subscribe(any()) } answers {
            arg<Observer<EventSources.Event>>(0).onNext(EventSources.Event.Shared)
            emptyDisposable()
        }
    }

    @DataProvider(name = "testData")
    fun testData(): Array<Array<Any>> {
        val dotnet = mockk<BuildRunnerSettings>(".NET") {
            every { runType } returns DotnetConstants.RUNNER_TYPE
            every { name } returns ".NET"
            every { runnerParameters } returns emptyMap()
        }

        val dotnetAndDocker = mockk<BuildRunnerSettings>(".NET with Docker") {
            every { runType } returns DotnetConstants.RUNNER_TYPE
            every { name } returns ".NET with Docker"
            every { runnerParameters } returns mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "abc")
        }

        val msBuild = mockk<BuildRunnerSettings>("MSBuild") {
            every { runType } returns "MSBuild"
            every { name } returns "MSBuild"
            every { runnerParameters } returns mapOf(DotnetConstants.PARAM_DOCKER_IMAGE to "abc")
        }

        val vs = mockk<BuildRunnerSettings>("VS") {
            every { runType } returns "VS.Solution"
            every { name } returns "VS"
        }

        val nugetInstall = mockk<BuildRunnerSettings>("Nuget Install") {
            every { runType } returns "jb.nuget.installer"
            every { name } returns "Nuget Install"
        }

        val other = mockk<BuildRunnerSettings>("Other") {
            every { runType } returns "xyz"
            every { name } returns "Other"
        }

        return arrayOf(
                arrayOf(listOf(dotnet), false, false),
                arrayOf(listOf(dotnetAndDocker), true, false),
                arrayOf(listOf(dotnetAndDocker, dotnet), true, false),
                arrayOf(listOf(msBuild, dotnetAndDocker, dotnet), true, true),
                arrayOf(listOf(vs, dotnetAndDocker, dotnet), true, true),
                arrayOf(listOf(nugetInstall, dotnetAndDocker, dotnet), true, true),
                arrayOf(listOf(vs, dotnetAndDocker, nugetInstall, dotnet), true, true),
                arrayOf(listOf(other, dotnetAndDocker), true, false),
                arrayOf(listOf(other, dotnet), false, false),
                arrayOf(listOf(other, dotnetAndDocker, dotnet), true, false))
    }

    @Test(dataProvider = "testData")
    fun shouldProvidePropertyAllowInternalCaches(runners: List<BuildRunnerSettings>, expectedAllowInternalCaches: Boolean, expectedHasWarning: Boolean) {
        // Given

        // When
        every { _build.buildRunners } returns runners
        every { _runnerContext.runType } returns runners.first().runType
        var env = createInstance()
        val actualAllowInternalCaches = env.allowInternalCaches

        // Then
        Assert.assertEquals(actualAllowInternalCaches, expectedAllowInternalCaches)
        if (expectedHasWarning) {
            verify { _loggerService.writeWarning(any()) }
        }
    }

    private fun createInstance(): NugetEnvironmentImpl {
        val instance =  NugetEnvironmentImpl(
                _buildStepContext,
                _loggerService)

        instance.subscribe(_sources)

        return instance
    }
}