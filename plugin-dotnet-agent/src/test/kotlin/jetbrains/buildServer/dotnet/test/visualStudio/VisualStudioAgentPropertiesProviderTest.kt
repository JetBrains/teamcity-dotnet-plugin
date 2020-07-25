package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.AgentPropertyType
import jetbrains.buildServer.visualStudio.VisualStudioAgentPropertiesProvider
import jetbrains.buildServer.visualStudio.VisualStudioInstance
import jetbrains.buildServer.visualStudio.VisualStudioProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioProvider1: VisualStudioProvider
    @MockK private lateinit var _visualStudioProvider2: VisualStudioProvider

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
        every { _visualStudioProvider1.getInstances() } returns sequenceOf(VisualStudioInstance(File("path1"), Version.parse("1.2.03"), Version.parse("2019")))
        every { _visualStudioProvider2.getInstances() } returns sequenceOf(VisualStudioInstance(File("path2"), Version.parse("1.2.03.4"), Version.parse("2003")))

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Visual Studio")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(
                        AgentProperty(AgentPropertyType.VisualStudio, "VS2019", "1.2.03"),
                        AgentProperty(AgentPropertyType.VisualStudio, "VS2019_Path", "path1"),
                        AgentProperty(AgentPropertyType.VisualStudio, "VS2003", "1.2.03.4"),
                        AgentProperty(AgentPropertyType.VisualStudio, "VS2003_Path", "path2")
                )
        )
    }

    private fun createInstance() =
            VisualStudioAgentPropertiesProvider(listOf(_visualStudioProvider1, _visualStudioProvider2))
}