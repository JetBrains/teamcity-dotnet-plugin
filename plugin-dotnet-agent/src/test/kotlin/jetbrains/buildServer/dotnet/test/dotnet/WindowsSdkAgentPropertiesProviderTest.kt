package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.WindowsSdkAgentPropertiesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class WindowsSdkAgentPropertiesProviderTest {
    @MockK private lateinit var _sdkInstanceProvider: ToolInstanceProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideAgentProperties() {
        // Given
        val propertiesProvider = createInstance()

        // When
        every { _sdkInstanceProvider.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.WindowsSDK, File("path1"), Version(10, 0, 18362), Version(10, 0), Platform.Default),
                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path2"), Version(4, 0, 123), Version(4, 0), Platform.x86),
                ToolInstance(ToolInstanceType.WindowsSDK, File("path3"), Version(8, 1, 345), Version(8, 1, 0, "A"), Platform.Default))

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Windows SDK")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(
                        AgentProperty(ToolInstanceType.WindowsSDK, "WindowsSDKv10.0", "10.0.18362"),
                        AgentProperty(ToolInstanceType.WindowsSDK, "WindowsSDKv10.0_Path", "path1"),
                        AgentProperty(ToolInstanceType.WindowsSDK, "WindowsSDKv8.1A", "8.1.345"),
                        AgentProperty(ToolInstanceType.WindowsSDK, "WindowsSDKv8.1A_Path", "path3")
                )
        )
    }

    private fun createInstance() =
            WindowsSdkAgentPropertiesProvider(_sdkInstanceProvider)
}