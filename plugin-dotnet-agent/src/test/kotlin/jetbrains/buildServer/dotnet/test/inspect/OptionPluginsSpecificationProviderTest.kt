package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.inspect.*
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class OptionPluginsSpecificationProviderTest {
    @MockK
    private lateinit var _pluginParametersProvider: PluginParametersProvider

    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun getPluginSpecificationsTestData() = arrayOf(
        arrayOf(emptyList<PluginParameter>(), null),
        arrayOf(listOf(PluginParameter("", "")), null),
        arrayOf(listOf(PluginParameter("   ", "   ")), null),
        arrayOf(listOf(PluginParameter("unknownType", "unknownSpec")), null),
        arrayOf(listOf(PluginParameter("folder", "folderSpec")), null),
        arrayOf(listOf(PluginParameter("file", "fileSpec")), null),
        arrayOf(listOf(PluginParameter("download", "downloadSpec")), "downloadSpec"),
        arrayOf(listOf(PluginParameter("abc", "downloadSpec")), null),
        arrayOf(listOf(PluginParameter("folder", "folderSpec"), PluginParameter("file", "fileSpec"), PluginParameter("download", "downloadSpec")), "downloadSpec"),
        arrayOf(listOf(PluginParameter("download", "downloadSpec1"), PluginParameter("download", "downloadSpec2")), "downloadSpec1;downloadSpec2"),
    )

    @Test(dataProvider = "getPluginSpecificationsTestData")
    fun `should provide proper plugin specifications in semicolon-separated-line format for download types`(
        pluginParameters: List<PluginParameter>,
        expectedPluginsSpec: String?
    ) {
        // arrange
        val provider = createInstance()
        every { _pluginParametersProvider.getPluginParameters() } answers { pluginParameters }
        every { _loggerService.writeWarning(any()) } answers { }

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginParametersProvider.getPluginParameters() }
    }

    @Test
    fun `should emit warning when facing unrecognized plugin and correctly process others`() {
        // arrange
        val provider = createInstance()
        val pluginsParameters = listOf(
            PluginParameter("download", "downloadSpec1"),
            PluginParameter("download", "downloadSpec2"),
            PluginParameter("unknownType", "unknownValue")
        )
        every { _pluginParametersProvider.getPluginParameters() } answers { pluginsParameters }
        every { _loggerService.writeWarning(any()) } answers { }
        val expectedPluginsSpec = "downloadSpec1;downloadSpec2"

        // act
        val actualPluginsSpec = provider.getPluginsSpecification()

        // assert
        assertEquals(actualPluginsSpec, expectedPluginsSpec)
        verify(exactly = 1) { _pluginParametersProvider.getPluginParameters() }
        verify(exactly = 1) { _loggerService.writeWarning(withArg { assertTrue(it.contains("unknownType")) }) }
    }

    private fun createInstance(): PluginsSpecificationProvider {
        return OptionPluginsSpecificationProvider(
            _pluginParametersProvider,
            _loggerService
        )
    }
}