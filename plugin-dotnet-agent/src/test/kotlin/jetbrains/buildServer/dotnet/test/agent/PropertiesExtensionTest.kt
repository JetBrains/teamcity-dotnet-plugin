

package jetbrains.buildServer.dotnet.test.agent

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.config.AgentParametersSupplier
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.testng.Assert
import org.testng.annotations.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PropertiesExtensionTest {
    private val mainThreadSurrogate = newSingleThreadContext("Main thread")
    @MockK private lateinit var _agentPropertiesProvider1: AgentPropertiesProvider
    @MockK private lateinit var _agentPropertiesProvider2: AgentPropertiesProvider
    @MockK private lateinit var _extensionHolder: ExtensionHolder

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @BeforeClass
    fun setUpClass() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterClass
    fun tearDownClass() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    @Test
    fun shouldAddOrUpdateConfigParams() {
        // Given
        every { _agentPropertiesProvider1.description } returns "1"
        every { _agentPropertiesProvider1.properties } returns sequenceOf(AgentProperty(ToolInstanceType.DotNetCLI, "prop1", "val1"), AgentProperty(ToolInstanceType.DotNetCLI, "prop", "val"))

        every { _agentPropertiesProvider2.description } returns "2"
        every { _agentPropertiesProvider2.properties } returns sequenceOf(AgentProperty(ToolInstanceType.DotNetCLI, "prop", "val"), AgentProperty(ToolInstanceType.DotNetCLI, "prop2", "val2"))

        val propertiesExtension = createInstance()

        // When
        val config = propertiesExtension.parameters

        // Then
        Assert.assertEquals(config.size, 3)
        Assert.assertEquals(config["prop1"], "val1")
        Assert.assertEquals(config["prop2"], "val2")
        Assert.assertEquals(config["prop"], "val")
    }

    private fun createInstance(): PropertiesExtension {
        every { _extensionHolder.registerExtension(AgentParametersSupplier::class.java, PropertiesExtension.PROPERTIES_EXTENSION_NAME, any()) } returns Unit

        val propertiesExtension = PropertiesExtension(Dispatchers.Main, listOf(_agentPropertiesProvider1, _agentPropertiesProvider2), _extensionHolder)

        verify { _extensionHolder.registerExtension(AgentParametersSupplier::class.java, PropertiesExtension.PROPERTIES_EXTENSION_NAME, propertiesExtension) }

        return propertiesExtension
    }
}