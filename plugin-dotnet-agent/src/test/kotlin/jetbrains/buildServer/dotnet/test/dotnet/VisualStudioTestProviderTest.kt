package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.VisualStudioTestProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class VisualStudioTestProviderTest {
    @MockK private lateinit var _baseToolInstanceProvider1: ToolInstanceProvider
    @MockK private lateinit var _baseToolInstanceProvider2: ToolInstanceProvider
    @MockK private lateinit var _visualStudioTestConsoleInstanceFactory: ToolInstanceFactory
    @MockK private lateinit var _msTestConsoleInstanceFactory: ToolInstanceFactory

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
        val vsTestTool2 = ToolInstance(ToolInstanceType.VisualStudioTest, File("path2"), Version(1, 2), Version(1, 2), Platform.Default)
        val msTestTool3 = ToolInstance(ToolInstanceType.MSTest, File("path3"), Version(1, 2), Version(1, 2), Platform.Default)
        val msTestTool4 = ToolInstance(ToolInstanceType.MSTest, File("path4"), Version(1, 2), Version(1, 2), Platform.Default)
        val vsTool5 = ToolInstance(ToolInstanceType.VisualStudio, File("path5"), Version(1, 2), Version(1, 2), Platform.Default)
        val vsTool6 = ToolInstance(ToolInstanceType.VisualStudio, File("path6"), Version(1, 2), Version(1, 2), Platform.Default)

        // When
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(any(), any(), any()) } returns null
        every { _visualStudioTestConsoleInstanceFactory.tryCreate(File("path5"), any(), any()) } returns vsTestTool2
        every { _msTestConsoleInstanceFactory.tryCreate(any(), any(), any()) } returns null
        every { _msTestConsoleInstanceFactory.tryCreate(File("path6"), any(), any()) } returns msTestTool4

        every { _baseToolInstanceProvider1.getInstances() } returns sequenceOf(msTestTool3, vsTool5)
        every { _baseToolInstanceProvider2.getInstances() } returns sequenceOf(vsTool6, vsTestTool1)

        var actualInstances = provider.getInstances().toList()

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

    private fun createInstance() =
            VisualStudioTestProvider(
                    listOf(_baseToolInstanceProvider1, _baseToolInstanceProvider2),
                    _visualStudioTestConsoleInstanceFactory,
                    _msTestConsoleInstanceFactory)
}