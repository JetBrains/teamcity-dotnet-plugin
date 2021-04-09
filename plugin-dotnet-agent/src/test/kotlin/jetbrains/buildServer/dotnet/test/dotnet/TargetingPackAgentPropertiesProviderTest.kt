package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetingPackAgentPropertiesProviderTest {
    @MockK private lateinit var _frameworksProvider: DotnetFrameworksProvider
    private val _path = File("path")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testProperties(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 0), _path)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version(2, 0), _path)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x64, Version(2, 0, 1), _path)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 0), _path),
                                DotnetFramework(Platform.x86, Version(2, 0), File("abc"))
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 0, 1), _path)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 0, 1, 3), _path)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 0), _path),
                                DotnetFramework(Platform.x86, Version(2, 0, 1), File("abc"))
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack2.0_Path", _path.path)
                        )
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(2, 1), _path)
                        ),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(3, 0), _path)
                        ),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        sequenceOf(
                                DotnetFramework(Platform.x86, Version(1, 0), _path)
                        ),
                        emptyList<AgentProperty>()
                )
        )
    }

    @Test(dataProvider = "testProperties")
    fun shouldProvideProperties(
            frameworks: Sequence<DotnetFramework>,
            expectedProperties: List<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance()

        // When
        every { _frameworksProvider.getFrameworks() } returns frameworks
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }


    private fun createInstance() =
            TargetingPackAgentPropertiesProvider(_frameworksProvider)
}