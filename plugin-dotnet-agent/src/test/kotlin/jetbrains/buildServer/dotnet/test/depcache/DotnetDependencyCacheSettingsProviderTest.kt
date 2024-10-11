package jetbrains.buildServer.dotnet.test.depcache

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheProvider
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheSettings
import jetbrains.buildServer.agent.cache.depcache.DependencyCacheSettingsProviderRegistry
import jetbrains.buildServer.cache.depcache.buildFeature.DependencyCacheBuildFeatureConstants
import jetbrains.buildServer.depcache.DotnetDependencyCacheConstants
import jetbrains.buildServer.depcache.DotnetDependencyCacheSettingsProvider
import jetbrains.buildServer.depcache.DotnetPackagesChangedInvalidator
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class DotnetDependencyCacheSettingsProviderTest {

    @MockK private lateinit var eventDispatcherMock: EventDispatcher<AgentLifeCycleListener>
    @MockK private lateinit var cacheSettingsProviderRegistryMock: DependencyCacheSettingsProviderRegistry
    @MockK private lateinit var buildMock: AgentRunningBuild
    private lateinit var instance: DotnetDependencyCacheSettingsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        clearAllMocks()
        every { buildMock.getSharedConfigParameters() } returns mapOf(DotnetDependencyCacheConstants.DEP_CACHE_ENABLED to "true")

        instance = DotnetDependencyCacheSettingsProvider(
            eventDispatcherMock, cacheSettingsProviderRegistryMock, mockk<DependencyCacheProvider>()
        )
    }

    @Test
    fun `should return settings and create invalidator when dependency cache build feature exists`() {
        // arrange
        val buildFeatureMock = mockk<AgentBuildFeature>()
        every { buildFeatureMock.type } returns DependencyCacheBuildFeatureConstants.DEPENDENCY_CACHE_BUILD_FEATURE_TYPE
        every { buildMock.getBuildFeaturesOfType(any()) } returns listOf(buildFeatureMock)
        val buildRunnerMock = mockk<BuildRunnerSettings>()
        every { buildRunnerMock.isEnabled } returns true
        every { buildRunnerMock.runType } returns DotnetConstants.RUNNER_TYPE
        every { buildMock.buildRunners } returns listOf(buildRunnerMock)

        // act
        val cacheSettings: List<DependencyCacheSettings> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertFalse(cacheSettings.isEmpty())
        Assert.assertEquals(cacheSettings.size, 1)
        Assert.assertNotNull(invalidator)
    }

    @Test
    fun `should not return settings and create invalidator when feature toggle is disabled`() {
        // arrange
        val buildFeatureMock = mockk<AgentBuildFeature>()
        every { buildFeatureMock.type } returns DependencyCacheBuildFeatureConstants.DEPENDENCY_CACHE_BUILD_FEATURE_TYPE
        every { buildMock.getBuildFeaturesOfType(any()) } returns listOf(buildFeatureMock)
        val buildRunnerMock = mockk<BuildRunnerSettings>()
        every { buildRunnerMock.isEnabled } returns true
        every { buildRunnerMock.runType } returns DotnetConstants.RUNNER_TYPE
        every { buildMock.buildRunners } returns listOf(buildRunnerMock)
        every { buildMock.getSharedConfigParameters()} returns mapOf(DotnetDependencyCacheConstants.DEP_CACHE_ENABLED to "false")

        // act
        val cacheSettings: List<DependencyCacheSettings?> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertTrue(cacheSettings.isEmpty())
        Assert.assertNull(invalidator)
    }

    @Test
    fun `should not return settings and create invalidator when there are no dotnet steps`() {
        // arrange
        val buildFeatureMock = mockk<AgentBuildFeature>()
        every { buildFeatureMock.type } returns DependencyCacheBuildFeatureConstants.DEPENDENCY_CACHE_BUILD_FEATURE_TYPE
        every { buildMock.getBuildFeaturesOfType(any()) } returns listOf(buildFeatureMock)
        val buildRunnerMock = mockk<BuildRunnerSettings>()
        every { buildRunnerMock.isEnabled } returns true
        every { buildRunnerMock.runType } returns "inaproppriateRunner"
        every { buildMock.buildRunners } returns listOf(buildRunnerMock)

        // act
        val cacheSettings: List<DependencyCacheSettings?> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertTrue(cacheSettings.isEmpty())
        Assert.assertNull(invalidator)
    }

    @Test
    fun `should not return settings and create invalidator when there are no enabled dotnet steps`() {
        // arrange
        val buildFeatureMock = mockk<AgentBuildFeature>()
        every { buildFeatureMock.type } returns DependencyCacheBuildFeatureConstants.DEPENDENCY_CACHE_BUILD_FEATURE_TYPE
        every { buildMock.getBuildFeaturesOfType(any()) } returns listOf(buildFeatureMock)
        val buildRunnerMock = mockk<BuildRunnerSettings>()
        every { buildRunnerMock.isEnabled } returns false
        every { buildRunnerMock.runType } returns DotnetConstants.RUNNER_TYPE
        every { buildMock.buildRunners } returns listOf(buildRunnerMock)

        // act
        val cacheSettings: List<DependencyCacheSettings?> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertTrue(cacheSettings.isEmpty())
        Assert.assertNull(invalidator)
    }

    @Test
    fun `should not return settings and create invalidator when no dependency cache build feature exists`() {
        // arrange
        every { buildMock.getBuildFeaturesOfType(any()) } returns emptyList()

        // act
        val cacheSettings: List<DependencyCacheSettings?> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertTrue(cacheSettings.isEmpty())
        Assert.assertNull(invalidator)
    }

    @Test
    fun `should unset invalidator when build finished`() {
        // arrange
        val buildFeatureMock = mockk<AgentBuildFeature>()
        every { buildFeatureMock.type } returns DependencyCacheBuildFeatureConstants.DEPENDENCY_CACHE_BUILD_FEATURE_TYPE
        every { buildMock.getBuildFeaturesOfType(any()) } returns listOf(buildFeatureMock)
        val buildRunnerMock = mockk<BuildRunnerSettings>()
        every { buildRunnerMock.isEnabled } returns true
        every { buildRunnerMock.runType } returns DotnetConstants.RUNNER_TYPE
        every { buildMock.buildRunners } returns listOf(buildRunnerMock)

        // act
        instance.register()
        val cacheSettings: List<DependencyCacheSettings?> = instance.getSettings(buildMock)
        val invalidator: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator
        val listenerSlot = slot<AgentLifeCycleListener>()

        verify { eventDispatcherMock.addListener(capture(listenerSlot)) }
        val actualListener = listenerSlot.captured
        Assert.assertNotNull(actualListener)
        actualListener.buildFinished(buildMock, BuildFinishedStatus.FINISHED_SUCCESS)
        val invalidatorAfterBuildFinished: DotnetPackagesChangedInvalidator? = instance.postBuildInvalidator

        // assert
        Assert.assertNotNull(cacheSettings)
        Assert.assertNotNull(invalidator)
        Assert.assertNull(invalidatorAfterBuildFinished)
    }

    @Test
    fun `should register when plugin context creates`() {
        // act
        instance.register()

        // assert
        verify { eventDispatcherMock.addListener(any()) }
        verify { cacheSettingsProviderRegistryMock.register(instance) }
    }

    @Test
    fun `should unregister when plugin context destroys`() {
        // act
        instance.unregister()

        // assert
        verify { eventDispatcherMock.removeListener(any()) }
        verify { cacheSettingsProviderRegistryMock.unregister(instance) }
    }
}