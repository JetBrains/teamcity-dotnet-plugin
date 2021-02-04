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
import jetbrains.buildServer.dotnet.MSTestAgentPropertiesProvider
import jetbrains.buildServer.dotnet.Platform
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class MSTestAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioTestInstanceProvider: ToolInstanceProvider

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
        every { _visualStudioTestInstanceProvider.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.VisualStudio, File("path1"), Version(16, 0, 18362), Version(16, 0), Platform.Default),
                ToolInstance(ToolInstanceType.MSTest, File("path"), Version(16, 0, 18362), Version(16, 2,1), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudioTest, File("path2"), Version(16, 0, 18362), Version(16, 2, 4), Platform.Default))

        // Then
        Assert.assertEquals(propertiesProvider.desription, "MSTest Console")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(AgentProperty(ToolInstanceType.MSTest, "teamcity.dotnet.mstest.16.2", "path"))
        )
    }

    private fun createInstance() =
            MSTestAgentPropertiesProvider(_visualStudioTestInstanceProvider)
}