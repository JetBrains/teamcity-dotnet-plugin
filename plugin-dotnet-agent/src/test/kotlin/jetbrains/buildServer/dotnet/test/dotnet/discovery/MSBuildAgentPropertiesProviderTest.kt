package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.ToolInstanceType.MSBuildTool
import jetbrains.buildServer.dotnet.discovery.MSBuildAgentPropertiesProvider
import jetbrains.buildServer.dotnet.discovery.MSBuildFileSystemAgentPropertiesProvider
import jetbrains.buildServer.dotnet.discovery.MSBuildRegistryAgentPropertiesProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MSBuildAgentPropertiesProviderTest {
    @MockK
    private lateinit var _msBuildFileSystemProvider: MSBuildFileSystemAgentPropertiesProvider

    @MockK
    private lateinit var _msBuildRegistryProvider: MSBuildRegistryAgentPropertiesProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<TestCase> {
        return arrayOf(
            // No duplicates, just merge
            TestCase(
                fileSystemProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools16.0_x86_Path", "path1"),
                    AgentProperty(MSBuildTool, "MSBuildTools16.0_x64_Path", "path2"),
                ),
                registryProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x86_Path", "path1"),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                ),
                expectedProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools16.0_x86_Path", "path1"),
                    AgentProperty(MSBuildTool, "MSBuildTools16.0_x64_Path", "path2"),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x86_Path", "path1"),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                )
            ),
            // Prioritize MSBuild from Visual Studio build tools
            TestCase(
                fileSystemProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path1"),
                    AgentProperty(
                        MSBuildTool,
                        "MSBuildTools17.0_x64_Path",
                        "C:\\Program Files (x86)\\Microsoft Visual Studio\\2022\\BuildTools\\MSBuild\\Current\\Bin",
                    ),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                ),
                registryProperties = listOf(),
                expectedProperties = listOf(
                    AgentProperty(
                        MSBuildTool,
                        "MSBuildTools17.0_x64_Path",
                        "C:\\Program Files (x86)\\Microsoft Visual Studio\\2022\\BuildTools\\MSBuild\\Current\\Bin",
                    ),
                )
            ),
            // Prioritize correctly when duplicates are from both file system and registry
            TestCase(
                fileSystemProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path1"),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                ),
                registryProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path3"),
                    AgentProperty(
                        MSBuildTool,
                        "MSBuildTools17.0_x64_Path",
                        "C:\\Program Files (x86)\\Microsoft Visual Studio\\2022\\BuildTools\\MSBuild\\Current\\Bin",
                    ),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path4"),
                ),
                expectedProperties = listOf(
                    AgentProperty(
                        MSBuildTool,
                        "MSBuildTools17.0_x64_Path",
                        "C:\\Program Files (x86)\\Microsoft Visual Studio\\2022\\BuildTools\\MSBuild\\Current\\Bin",
                    ),
                )
            ),
            // Deprioritize MSBuild from Microsoft SQL Server Management Studio
            TestCase(
                fileSystemProperties = listOf(
                    AgentProperty(
                        MSBuildTool,
                        "MSBuildTools17.0_x64_Path",
                        "C:\\Program Files\\Microsoft SQL Server Management Studio 21\\Release\\MSBuild\\Current\\Bin",
                    ),
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                ),
                registryProperties = listOf(),
                expectedProperties = listOf(
                    AgentProperty(MSBuildTool, "MSBuildTools17.0_x64_Path", "path2"),
                )
            ),
        )
    }

    data class TestCase(
        val fileSystemProperties: List<AgentProperty>,
        val registryProperties: List<AgentProperty>,
        val expectedProperties: List<AgentProperty>,
    )

    @Test(dataProvider = "testData")
    fun testProperties(testCase: TestCase) {
        // given
        val provider = MSBuildAgentPropertiesProvider(_msBuildFileSystemProvider, _msBuildRegistryProvider)

        every { _msBuildFileSystemProvider.properties } returns testCase.fileSystemProperties.asSequence()
        every { _msBuildRegistryProvider.properties } returns testCase.registryProperties.asSequence()

        // when, then
        Assert.assertEquals(provider.properties.toList(), testCase.expectedProperties)
    }
}