package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.inspect.*
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PluginDescriptorsProviderImplTest {
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
    fun getPluginParametersTestData() = arrayOf(
        arrayOf(
            null,
            listOf<PluginDescriptor>()
        ),
        arrayOf(
            listOf<String>(),
            listOf<PluginDescriptor>()
        ),
        arrayOf(
            listOf(
                "    ",
                "",
                "\t",
                " \r",
            ),
            listOf<PluginDescriptor>()
        ),
        arrayOf(
            listOf(
                "source1 value1",
                "source2 \"value2\"",
                "source3\tvalue3",
                "   source4   value4   ",
                "   source5   multi part value  ",
                "    ",
                "Plugin/1.2.3",
                "  Plugin  ",
                "Plugin1${System.lineSeparator()}Plugin2"
            ),
            listOf(
                PluginDescriptor(PluginDescriptorType.SOURCE, "source1 value1"),
                PluginDescriptor(PluginDescriptorType.SOURCE, "source2 \"value2\""),
                PluginDescriptor(PluginDescriptorType.SOURCE, "source3\tvalue3"),
                PluginDescriptor(PluginDescriptorType.SOURCE, "source4   value4"),
                PluginDescriptor(PluginDescriptorType.SOURCE, "source5   multi part value"),
                PluginDescriptor(PluginDescriptorType.ID, "Plugin/1.2.3"),
                PluginDescriptor(PluginDescriptorType.ID, "Plugin"),
                PluginDescriptor(PluginDescriptorType.ID, "Plugin1"),
                PluginDescriptor(PluginDescriptorType.ID, "Plugin2"),
            )
        ),
    )

    @Test(dataProvider = "getPluginParametersTestData")
    fun `should provide correctly typed plugin parameters when lines are correct`(
        pluginsParameterLines: List<String>?,
        expectedPluginDescriptors: List<PluginDescriptor>
    ) {
        // arrange
        val provider = createInstance()
        val pluginsParameter = pluginsParameterLines?.joinToString(System.lineSeparator())
        every { _parametersService.tryGetParameter(ParameterType.Runner, InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS) } answers { pluginsParameter }
        every { _loggerService.writeWarning(any()) } answers { }

        // act
        val actualPluginParameters = provider.getPluginDescriptors()

        // assert
        assertEquals(actualPluginParameters, expectedPluginDescriptors)
        verify(exactly = 1) { _parametersService.tryGetParameter(any(), any()) }
        verify(exactly = 0) { _loggerService.writeWarning(withArg { any() }) }
    }

    @DataProvider
    fun hasPluginParametersData(): Array<Array<out Any?>> {
        return arrayOf(
            arrayOf("some plugin${System.lineSeparator()}other plugin", true),
            arrayOf("some plugin", true),
            arrayOf("  \t \r  ", false),
            arrayOf("", false),
            arrayOf(null, false),
        )
    }

    fun createInstance() = PluginDescriptorsProviderImpl(_parametersService, _loggerService)
}