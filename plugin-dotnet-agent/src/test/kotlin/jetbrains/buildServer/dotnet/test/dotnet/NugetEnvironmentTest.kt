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
    @MockK private lateinit var _buildStartedSource: Observable<EventSources.BuildStartedEvent>

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)

        every { _buildStepContext.isAvailable } returns true
        every { _buildStepContext.runnerContext } returns _runnerContext
        every { _runnerContext.build } returns _build
        every { _loggerService.writeWarning(any()) } returns Unit
        every { _sources.buildStartedSource } returns _buildStartedSource
        every { _buildStartedSource.subscribe(any()) } answers {
            arg<Observer<EventSources.BuildStartedEvent>>(0).onNext(EventSources.BuildStartedEvent(_build))
            emptyDisposable()
        }
    }

    @DataProvider(name = "testData")
    fun testData(): Array<Array<Any>> {
        val dotnet = mockk<BuildRunnerSettings>(".NET") {
            every { runType } returns DotnetConstants.RUNNER_TYPE
            every { runnerParameters } returns emptyMap()
        }

        val dotnetAndDocker = mockk<BuildRunnerSettings>(".NET with Docker") {
            every { runType } returns DotnetConstants.RUNNER_TYPE
            every { runnerParameters } returns mapOf(NugetEnvironmentImpl.DOCKER_WRAPPER_IMAGE_PARAM to "abc")
        }

        val msBuild = mockk<BuildRunnerSettings>("MSBuild") {
            every { runType } returns "MSBuild"
            every { runnerParameters } returns mapOf(NugetEnvironmentImpl.DOCKER_WRAPPER_IMAGE_PARAM to "abc")
        }

        val vs = mockk<BuildRunnerSettings>("VS") {
            every { runType } returns "VS.Solution"
        }

        val nugetInstall = mockk<BuildRunnerSettings>("Nuget Install") {
            every { runType } returns "jb.nuget.installer"
        }

        val other = mockk<BuildRunnerSettings>("Other") {
            every { runType } returns "xyz"
        }

        return arrayOf(
                arrayOf(emptyList<BuildRunnerSettings>(), false, false),
                arrayOf(listOf(dotnet), false, false),
                arrayOf(listOf(dotnetAndDocker), true, false),
                arrayOf(listOf(dotnetAndDocker, dotnet), true, false),
                arrayOf(listOf(dotnetAndDocker, msBuild, dotnet), true, true),
                arrayOf(listOf(dotnetAndDocker, vs, dotnet), true, true),
                arrayOf(listOf(dotnetAndDocker, nugetInstall, dotnet), true, true),
                arrayOf(listOf(dotnetAndDocker, vs, nugetInstall, dotnet), true, true),
                arrayOf(listOf(dotnetAndDocker, other), true, false),
                arrayOf(listOf(dotnet, other), false, false),
                arrayOf(listOf(dotnetAndDocker, other, dotnet), true, false))
    }

    @Test(dataProvider = "testData")
    fun shouldProvidePropertyAllowInternalCaches(runners: List<BuildRunnerSettings>, expectedAllowInternalCaches: Boolean, expectedHasWarning: Boolean) {
        // Given

        // When
        every { _build.buildRunners } returns runners
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