

package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.discovery.DotnetSdkAgentPropertiesProvider
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetSdkAgentPropertiesProviderTest {
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _dotnetVersionProvider: DotnetVersionProvider
    @MockK private lateinit var _dotnetSdksProvider: DotnetSdksProvider
    @MockK private lateinit var _toolProvider: ToolProvider
    @MockK private lateinit var _versionEnumerator: VersionEnumerator
    private val _toolPath = Path("dotnet")
    private val _workPath = Path("work")
    private val _configPathProperty = AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, File(_toolPath.path).canonicalPath)
    private val _configNameProperty = AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, "1.0.1")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<List<Any>>> {
        return arrayOf(
                arrayOf(
                        emptyList<DotnetSdk>(),
                        listOf(_configPathProperty, _configNameProperty)),

                arrayOf(
                        listOf(
                                DotnetSdk(File("1.2.3"), Version(1, 2, 3))
                        ),
                        listOf(
                                _configPathProperty, _configNameProperty,
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_PREFIX_CORE_SDK}1.2${DotnetConstants.CONFIG_SUFFIX_PATH}", File("1.2.3").absolutePath),
                                AgentProperty(ToolInstanceType.DotNetSDK, "${DotnetConstants.CONFIG_PREFIX_CORE_SDK}1.2.3${DotnetConstants.CONFIG_SUFFIX_PATH}", File("1.2.3").absolutePath)))
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideAgentProperties(
        sdks: List<DotnetSdk>,
        expectedProperties: List<AgentProperty>) {
        // Given
        every { _toolProvider.getPath(DotnetConstants.EXECUTABLE) } returns _toolPath.path
        every { _pathsService.getPath(PathType.Work) } returns File(_workPath.path)
        every { _dotnetVersionProvider.getVersion(_toolPath, _workPath) } returns Version(1, 0, 1)
        every { _dotnetSdksProvider.getSdks(File(_toolPath.path)) } returns sdks.asSequence()
        every { _versionEnumerator.enumerate<DotnetSdk>(any()) } answers {
            sequence {
                for (sdk in arg<Sequence<DotnetSdk>>(0)) {
                    yield(Pair("${sdk.version.major}.${sdk.version.minor}", sdk))
                    yield(Pair(sdk.version.toString(), sdk))
                }
            }
        }

        val propertiesProvider = createInstance()
        val actualDescription = propertiesProvider.description
        val actualProperties = propertiesProvider.properties.toList()

        // When
        Assert.assertEquals(actualDescription, ".NET SDK")
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance() = DotnetSdkAgentPropertiesProvider(
            _toolProvider,
            _dotnetVersionProvider,
            _dotnetSdksProvider,
            _pathsService,
            _versionEnumerator)
}