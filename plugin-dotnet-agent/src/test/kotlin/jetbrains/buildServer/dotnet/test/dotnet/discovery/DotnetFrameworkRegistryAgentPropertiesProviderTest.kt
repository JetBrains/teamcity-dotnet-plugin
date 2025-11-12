

package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.discovery.DotnetFrameworkRegistryAgentPropertiesProvider
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFramework
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFrameworksProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetFrameworkRegistryAgentPropertiesProviderTest {
    @MockK private lateinit var _dotnetFrameworksProvider: DotnetFrameworksProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<Sequence<Any>>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version(1, 2, 3), File("dotnetPath"))
                        ),
                        sequenceOf(
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64", "1.2.3"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2.3_x64_Path", "dotnetPath")
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version.parse("1.2.03"), File("dotnetPath"))
                        ),
                        sequenceOf(
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64", "1.2.03"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2.03_x64_Path", "dotnetPath")
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version.parse("4.0.1"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("1.2.03"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.9.023"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.3.6"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.9"), File("dotnetPath"))
                        ),
                        sequenceOf(
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64", "1.2.03"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2.03_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9_x64", "4.9.023"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9.023_x64_Path", "dotnetPath")
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version.parse("4.0.1"), File("dotnetPath")),
                                DotnetFramework(Platform.x86, Version.parse("4.0.1"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("1.2.03"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.9.023"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.3.6"), File("dotnetPath")),
                                DotnetFramework(Platform.x64, Version.parse("4.9"), File("dotnetPath"))
                        ),
                        sequenceOf(
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.0_x86", "4.0.1"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.0_x86_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.0.1_x86_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64", "1.2.03"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework1.2.03_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9_x64", "4.9.023"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9_x64_Path", "dotnetPath"),
                                AgentProperty(ToolInstanceType.DotNetFramework, "DotNetFramework4.9.023_x64_Path", "dotnetPath")
                        )
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideAgentProperties(frameworks: Sequence<DotnetFramework>, expectedProperties: Sequence<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance()

        // When
        every { _dotnetFrameworksProvider.getFrameworks() } returns frameworks

        // Then
        Assert.assertEquals(propertiesProvider.description, "Dotnet Framework in registry")
        Assert.assertEquals(propertiesProvider.properties.sortedBy { it.name }.toList(), expectedProperties.sortedBy { it.name }.toList())
    }

    private fun createInstance() =
            DotnetFrameworkRegistryAgentPropertiesProvider(_dotnetFrameworksProvider)
}