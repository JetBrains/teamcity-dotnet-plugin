package jetbrains.buildServer.dotnet.test.depcache

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildAgent
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.depcache.DotnetCacheDirectoryAllowedListAdder
import jetbrains.buildServer.dotnet.DotnetRunnerCacheDirectoryProvider
import jetbrains.buildServer.util.EventDispatcher
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetCacheDirectoryAllowedListAdderTest {

    @MockK(relaxed = true)
    private lateinit var _eventDispatcher: EventDispatcher<AgentLifeCycleListener>
    @MockK
    private lateinit var _dotnetRunnerCacheDirectoryProvider: DotnetRunnerCacheDirectoryProvider

    @DataProvider
    fun getAllowedListTestData(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(
                "/some/path", File("/path/to/cache"), "/some/path" + "," + File("/path/to/cache")
            ),
            arrayOf(
                "D:\\some\\path", File("C:\\path\\to\\cache"), "D:\\some\\path" + "," + File("C:\\path\\to\\cache")
            ),
            arrayOf(
                "", File("/path/to/cache"), File("/path/to/cache").toString()
            ),
            arrayOf(
                " ", File("/path/to/cache"), File("/path/to/cache").toString()
            ),
            arrayOf(
                "\t", File("/path/to/cache"), File("/path/to/cache").toString()
            ),
            arrayOf(
                null, File("/path/to/cache"), File("/path/to/cache").toString()
            )
        )
    }

    @BeforeMethod
    fun setup() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test(dataProvider = "getAllowedListTestData")
    fun `should append allowed list when agent configuration loaded`(
        initialAllowedList: String?,
        cacheDirectory: File,
        expectedAllowedList: String
    ) {
        // arrange
        val agentConfiguration = mockk<BuildAgentConfiguration>(relaxed = true)
        every { _dotnetRunnerCacheDirectoryProvider.getDotnetRunnerCacheDirectory(agentConfiguration) } returns cacheDirectory
        val allowedListParamName = "teamcity.artifactDependenciesResolution.allowedList"
        every { agentConfiguration.configurationParameters } returns mapOf(
            allowedListParamName to initialAllowedList
        )
        val buildAgent = mockk<BuildAgent>()
        every { buildAgent.configuration } returns agentConfiguration
        val actualAllowedListSlot = slot<String>()
        val instance = create()

        // act
        instance.afterAgentConfigurationLoaded(buildAgent)

        // assert
        verify(exactly = 1) { agentConfiguration.configurationParameters }
        verify(exactly = 1) { agentConfiguration.addConfigurationParameter(allowedListParamName, capture(actualAllowedListSlot)) }
        val actualAllowedList = actualAllowedListSlot.captured;
        Assert.assertEquals(actualAllowedList, expectedAllowedList);
    }

    @Test
    fun `should add itself as listener to dispatcher when register`() {
        // arrange
        val instance = create()

        // act
        instance.register()

        // assert
        verify(exactly = 1) { _eventDispatcher.addListener(instance) }
    }

    @Test
    fun `should remove itself from listeners in dispatcher when unregister`() {
        // arrange
        val instance = create()

        // act
        instance.unregister()

        // assert
        verify(exactly = 1) { _eventDispatcher.removeListener(instance) }
    }

    private fun create() = DotnetCacheDirectoryAllowedListAdder(
        _eventDispatcher, _dotnetRunnerCacheDirectoryProvider
    )
}