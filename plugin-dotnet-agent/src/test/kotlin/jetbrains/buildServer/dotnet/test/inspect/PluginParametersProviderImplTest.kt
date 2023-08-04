package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.InspectCodeConstants
import jetbrains.buildServer.inspect.PluginParameter
import jetbrains.buildServer.inspect.PluginParametersProvider
import jetbrains.buildServer.inspect.PluginParametersProviderImpl
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PluginParametersProviderImplTest {
    @MockK
    private lateinit var _parametersService: ParametersService

    @MockK
    private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun getPluginParametersTestData(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf(
                null,
                listOf<PluginParameter>()
            ),
            arrayOf(
                listOf<String>(),
                listOf<PluginParameter>()
            ),
            arrayOf(
                listOf(
                    "type1 value1",
                    "type2 value2",
                    "type3\tvalue3",
                    "   type4   value4   ",
                    "   type5   \"multi part value\"  ",
                    "    ",
                    "asdsadasdasadd",
                    "  asdsadasdasads  ",
                    "aasdasdasd\nadsasdasddsa"
                ),
                listOf(
                    PluginParameter("type1", "value1"),
                    PluginParameter("type2", "value2"),
                    PluginParameter("type3", "value3"),
                    PluginParameter("type4", "value4"),
                    PluginParameter("type5", "\"multi part value\"")
                )
            ),
        )
    }

    @Test(dataProvider = "getPluginParametersTestData")
    fun `should provide plugin parameters ignoring wrong lines`(
        pluginsParameterLines: List<String>?,
        expectedPluginParameters: List<PluginParameter>
    ) {
        // arrange
        val provider = createInstance()
        val pluginsParameter = pluginsParameterLines?.joinToString(System.lineSeparator())
        every { _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS) } answers { pluginsParameter }
        every { _loggerService.writeWarning(any()) } answers { }

        // act
        val actualPluginParameters = provider.getPluginParameters()

        // assert
        assertEquals(actualPluginParameters, expectedPluginParameters)
        verify(exactly = 1) { _parametersService.tryGetParameter(any(), any()) }
    }

    @Test
    fun `should emit warning when faced with incorrect plugin parameter and process others`() {
        // arrange
        val provider = createInstance()
        val pluginsParameterLines = listOf(
            "type1 value1",
            "adsasdasdasds",
            "type2 value2"
        )
        val expectedPluginParameters = listOf(
            PluginParameter("type1", "value1"),
            PluginParameter("type2", "value2")
        )
        val pluginsParameter = pluginsParameterLines.joinToString(System.lineSeparator())
        every { _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS) } answers { pluginsParameter }
        every { _loggerService.writeWarning(any()) } answers { }

        // act
        val actualPluginParameters = provider.getPluginParameters()

        // assert
        assertEquals(actualPluginParameters, expectedPluginParameters)
        verify(exactly = 1) { _parametersService.tryGetParameter(any(), any()) }
        verify(exactly = 1) { _loggerService.writeWarning(withArg { assertTrue(it.contains("adsasdasdasds")) }) }
    }

    @DataProvider
    fun hasPluginParametersData(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf("some plugin${System.lineSeparator()}other plugin", true),
            arrayOf("some plugin", true),
            arrayOf("  \t  ", false),
            arrayOf("", false),
            arrayOf(null, false),
        )
    }

    @Test(dataProvider = "hasPluginParametersData")
    fun `should indicate plugins parameter existence`(pluginParameters: String?, expectedResult: Boolean) {
        // arrange
        val provider = createInstance()
        every { _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS) } answers { pluginParameters }

        // act
        val hasPluginParameters = provider.hasPluginParameters()

        // assert
        assertEquals(hasPluginParameters, expectedResult)
        verify(exactly = 1) { _parametersService.tryGetParameter(any(), any()) }
    }

    fun createInstance(): PluginParametersProvider {
        return PluginParametersProviderImpl(_parametersService, _loggerService)
    }
}