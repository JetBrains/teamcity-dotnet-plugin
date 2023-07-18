package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.*
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
    @MockK
    private lateinit var _toolProvider: ToolProvider

    @MockK
    private lateinit var _fileBasedDotnetWorkloadProvider: DotnetWorkloadProvider

    @MockK
    private lateinit var _registryBasedDotnetWorkloadProvider: DotnetWorkloadProvider

    private val _toolPath = Path("dotnet")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _toolProvider.getPath(EXECUTABLE) } returns _toolPath.path
    }

    @Test
    fun `should have correct description`() {
        // Then
        assertEquals(createInstance().desription, ".NET Workload")
    }

    @DataProvider
    fun `workloads to expected agent properties`(): Array<Array<List<Any>>> {
        return arrayOf(
            arrayOf(
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                    DotnetWorkload("android", Version.parse("7.0.100")),
                    DotnetWorkload("ios", Version.parse("6.0.100")),
                ),
                listOf(
                    AgentProperty(DotNetCLI, "DotNetWorkloads_6.0.100", "android,ios"),
                    AgentProperty(DotNetCLI, "DotNetWorkloads_7.0.100", "android")
                ),
            ),
            arrayOf(
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                    DotnetWorkload("android", Version.parse("6.0.100")),
                ),
                listOf(
                    AgentProperty(DotNetCLI, "DotNetWorkloads_6.0.100", "android")
                ),
            ),
            arrayOf(emptyList(), listOf())
        )
    }

    @Test(dataProvider = "workloads to expected agent properties")
    fun `should provide DotNetWorkloads agent properties retrieved from file system`(
        installedWorkloads: List<DotnetWorkload>,
        expectedAgentProperties: List<AgentProperty>
    ) {
        // given
        every { _fileBasedDotnetWorkloadProvider.getInstalledWorkloads(File(_toolPath.path)) } returns installedWorkloads
        if (installedWorkloads.isEmpty())
            every { _registryBasedDotnetWorkloadProvider.getInstalledWorkloads(File(_toolPath.path)) } returns installedWorkloads
        val provider = createInstance()

        // when
        val actualProperties = provider.properties.toList()

        // then
        assertEquals(actualProperties, expectedAgentProperties)
        if (installedWorkloads.isNotEmpty())
            verify { _registryBasedDotnetWorkloadProvider wasNot Called }
    }

    @Test(dataProvider = "workloads to expected agent properties")
    fun `should provide DotNetWorkloads agent properties retrieved from registry`(
        installedWorkloads: List<DotnetWorkload>,
        expectedAgentProperties: List<AgentProperty>
    ) {
        // given
        every { _fileBasedDotnetWorkloadProvider.getInstalledWorkloads(File(_toolPath.path)) } returns emptyList()
        every { _registryBasedDotnetWorkloadProvider.getInstalledWorkloads(File(_toolPath.path)) } returns installedWorkloads
        val provider = createInstance()

        // when
        val actualProperties = provider.properties.toList()

        // then
        assertEquals(actualProperties, expectedAgentProperties)
    }

    private fun createInstance() = DotnetWorkloadAgentPropertiesProvider(
        _toolProvider,
        _fileBasedDotnetWorkloadProvider,
        _registryBasedDotnetWorkloadProvider
    )
}