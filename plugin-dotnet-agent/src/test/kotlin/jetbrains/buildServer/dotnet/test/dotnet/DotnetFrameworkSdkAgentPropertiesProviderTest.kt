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
import jetbrains.buildServer.dotnet.DotnetFrameworkSdkAgentPropertiesProvider
import jetbrains.buildServer.dotnet.Platform
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworkSdkAgentPropertiesProviderTest {
    @MockK private lateinit var _sdkInstanceProvider1: ToolInstanceProvider
    @MockK private lateinit var _sdkInstanceProvider2: ToolInstanceProvider

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
        every { _sdkInstanceProvider1.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.WindowsSDK, File("path1"), Version(10, 0, 18362), Version(10, 0), Platform.Default),
                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path2"), Version(4, 0, 123), Version.parse("4.0"), Platform.x86))

        // When
        every { _sdkInstanceProvider2.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.DotNetFrameworkSDK, File("path3"), Version(3, 5, 456, 1), Version.parse("3.5"), Platform.x64),
                ToolInstance(ToolInstanceType.WindowsSDK, File("path4"), Version(8, 1, 345), Version(8, 1, 0, "A"), Platform.Default))

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Dotnet Framework SDK")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(
                        AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK4.0_x86", "4.0.123"),
                        AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK4.0_x86_Path", "path2"),
                        AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK3.5_x64", "3.5.456.1"),
                        AgentProperty(ToolInstanceType.DotNetFrameworkSDK, "DotNetFrameworkSDK3.5_x64_Path", "path3")
                )
        )
    }

    private fun createInstance() =
            DotnetFrameworkSdkAgentPropertiesProvider(listOf(_sdkInstanceProvider1, _sdkInstanceProvider2))
}