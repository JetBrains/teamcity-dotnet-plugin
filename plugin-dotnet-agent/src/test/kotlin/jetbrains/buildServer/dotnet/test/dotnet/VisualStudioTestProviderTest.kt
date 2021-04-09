package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.VisualStudioTestProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioTestProviderTest {
    @MockK private lateinit var _baseToolInstanceProvider1: ToolInstanceProvider
    @MockK private lateinit var _baseToolInstanceProvider2: ToolInstanceProvider
    @MockK private lateinit var _visualStudioTestConsoleInstanceFactory: ToolInstanceFactory
    @MockK private lateinit var _msTestConsoleInstanceFactory: ToolInstanceFactory
    @MockK private lateinit var _parametersService: ParametersService

    private val _vsTestTool = ToolInstance(ToolInstanceType.VisualStudioTest, File("path1"), Version(1, 2), Version(1, 2), Platform.Default)
    private val _msTestTool = ToolInstance(ToolInstanceType.MSTest, File("path2"), Version(1, 2), Version(1, 2), Platform.Default)
    private val _vsTestToolA = ToolInstance(ToolInstanceType.VisualStudioTest, File("pathA"), Version(1, 2), Version(1, 2), Platform.Default)
    private val _vsTestToolB = ToolInstance(ToolInstanceType.VisualStudioTest, File("pathB"), Version(2, 2), Version(2, 2), Platform.Default)


    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    public fun shouldCreateInstance()
    {
        // Given
        val provider = createInstance()
        val vsTestTool1 = ToolInstance(ToolInstanceType.VisualStudioTest, File("path1"), Version(1, 2), Version(1, 2), Platform.Default)
        val vsTestTool2 = ToolInstance(ToolInstanceType.VisualStudioTest, File("path2"), Version(2, 2), Version(2, 2), Platform.Default)
        val msTestTool3 = ToolInstance(ToolInstanceType.MSTest, File("path3"), Version(1, 2), Version(1, 2), Platform.Default)
        val msTestTool4 = ToolInstance(ToolInstanceType.MSTest, File("path4"), Version(2, 2), Version(2, 2), Platform.Default)
        val vsTool5 = ToolInstance(ToolInstanceType.VisualStudio, File("path5"), Version(1, 2), Version(1, 2), Platform.Default)
        val vsTool6 = ToolInstance(ToolInstanceType.VisualStudio, File("path6"), Version(2, 2), Version(2, 2), Platform.Default)

        // When
        every { _parametersService.getParameterNames(ParameterType.Internal) } returns emptySequence()
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(any(), any(), any()) } returns null
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(File("path5"), any(), any()) } returns vsTestTool2
        every { _msTestConsoleInstanceFactory.tryCreate(any(), any(), any()) } returns null
        every { _msTestConsoleInstanceFactory.tryCreate(File("path6"), any(), any()) } returns msTestTool4

        every { _baseToolInstanceProvider1.getInstances() } returns listOf(msTestTool3, vsTool5)
        every { _baseToolInstanceProvider2.getInstances() } returns listOf(vsTool6, vsTestTool1)

        var actualInstances = provider.getInstances()

        // Then
        Assert.assertEquals(
                actualInstances,
                listOf(
                        msTestTool3,
                        vsTestTool2,
                        msTestTool4,
                        vsTestTool1
                ))
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.16.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestToolB,
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "TeamCity.Dotnet.VSTest.16.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestToolB,
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.16.0.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestToolB,
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.16.install.dir" to "pathA"
                        ),
                        sequenceOf(
                                _vsTestToolA,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.16.0.install.dir" to "pathA",
                                "teamcity.dotnet.vstest.15.0.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestToolA,
                                _vsTestToolB,
                                _msTestTool
                        )
                ),
                arrayOf(
                        emptyMap<String, String>(),
                        sequenceOf(
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.16.install.dir" to "pathC"
                        ),
                        sequenceOf(
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "teamcity.dotnet.vstest.abc.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "dotnet.vstest.16.1.install.dir" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestTool,
                                _msTestTool
                        )
                ),
                arrayOf(
                        mapOf(
                                "abc" to "pathB"
                        ),
                        sequenceOf(
                                _vsTestTool,
                                _msTestTool
                        )
                )
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideToolsFromConfig(internalProps: Map<String, String>, expectedTools: Sequence<ToolInstance>)
    {
        // Given
        val provider = createInstance()
        every { _parametersService.getParameterNames(ParameterType.Internal) } returns internalProps.keys.asSequence()
        every { _parametersService.tryGetParameter(ParameterType.Internal, any()) } answers { internalProps[arg(1)] }
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(any(), any(), any()) } returns null
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(File("pathA"), any(), any()) } returns _vsTestToolA
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(File("pathB"), any(), any()) } returns _vsTestToolB

        // When
        every { _baseToolInstanceProvider1.getInstances() } returns listOf(_vsTestTool)
        every { _baseToolInstanceProvider2.getInstances() } returns listOf(_msTestTool)
        var actualInstances = provider.getInstances().sortedBy { it.toString() }.toList()

        // Then
        Assert.assertEquals(actualInstances, expectedTools.sortedBy { it.toString() }.toList())
    }

    private fun createInstance() =
            VisualStudioTestProvider(
                    listOf(_baseToolInstanceProvider1, _baseToolInstanceProvider2),
                    _visualStudioTestConsoleInstanceFactory,
                    _msTestConsoleInstanceFactory,
                    _parametersService)
}