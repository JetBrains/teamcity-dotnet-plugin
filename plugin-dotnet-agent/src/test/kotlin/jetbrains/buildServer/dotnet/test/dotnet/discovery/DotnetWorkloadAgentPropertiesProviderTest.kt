package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolInstanceType.DotNetCLI
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetConstants.EXECUTABLE
import jetbrains.buildServer.dotnet.DotnetWorkload
import jetbrains.buildServer.dotnet.DotnetWorkloadProvider
import jetbrains.buildServer.dotnet.discovery.DotnetWorkloadAgentPropertiesProvider
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetWorkloadAgentPropertiesProviderTest {
    @MockK private lateinit var _toolProvider: ToolProvider
    @MockK private lateinit var _dotnetWorkloadProvider: DotnetWorkloadProvider
    private val _toolPath = Path("dotnet")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _toolProvider.getPath(EXECUTABLE) } returns _toolPath.path
    }

    @Test
    fun shouldHaveCorrectDescription() {
        // Then
        assertEquals(createInstance().desription, ".NET Workload")
    }

    @DataProvider
    fun testDataInstalledWorkloads(): Array<Array<List<Any>>> {
        return arrayOf(
            arrayOf(
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                    DotnetWorkload("android", Version.parse("7.0.100")),
                    DotnetWorkload("ios", Version.parse("6.0.100")),
                ),
                listOf(
                    AgentProperty(DotNetCLI, "DotNetInstalledWorkloads_6.0.100", "android,ios"),
                    AgentProperty(DotNetCLI, "DotNetInstalledWorkloads_7.0.100", "android")
                ),
            ),
            arrayOf(emptyList(), listOf())
        )
    }

    @Test(dataProvider = "testDataInstalledWorkloads")
    fun shouldReturnDotnetInstalledWorkloadsAgentProperty(
        installedWorkloads: List<DotnetWorkload>,
        expectedAgentProperties: List<AgentProperty>
    ) {
        // Given
        every { _dotnetWorkloadProvider.getInstalledWorkloads(File(_toolPath.path)) } returns installedWorkloads
        val provider = createInstance()

        // When
        val actualProperties = provider.properties.toList()

        // Then
        assertEquals(actualProperties, expectedAgentProperties)
    }

    private fun createInstance() = DotnetWorkloadAgentPropertiesProvider(
        _toolProvider,
        _dotnetWorkloadProvider
    )
}