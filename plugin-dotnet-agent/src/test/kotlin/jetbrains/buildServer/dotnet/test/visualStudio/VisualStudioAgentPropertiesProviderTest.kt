package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.visualStudio.VisualStudioAgentPropertiesProvider
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class VisualStudioAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioProvider1: ToolInstanceProvider
    @MockK private lateinit var _visualStudioProvider2: ToolInstanceProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideAgentProperties() {
        // Given
        val propertiesProvider = createInstance(VirtualFileSystemService().addFile(File("path1/devenv.exe"), VirtualFileSystemService.Attributes()))

        // When
        every { _visualStudioProvider1.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.VisualStudio, File("path1"), Version.parse("1.2.03"), Version.parse("2019"), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudioTest, File("path3"), Version.parse("1.4"), Version.parse("2017"), Platform.Default)
        )
        every { _visualStudioProvider2.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.MSTest, File("path3"), Version.parse("1.4"), Version.parse("2017"), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudio, File("path2"), Version.parse("1.2.03.4"), Version.parse("2003"), Platform.Default)
        )

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Visual Studio")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(
                        AgentProperty(ToolInstanceType.VisualStudio, "VS2019", "1.2.03"),
                        AgentProperty(ToolInstanceType.VisualStudio, "VS2019_Path", "path1")
                )
        )
    }

    @Test
    fun shouldProvideAgentPropertiesWhenSeveralVSInstallationWithSameVersionButTheFirstHasNoDevenv() {
        // Given
        val propertiesProvider = createInstance(VirtualFileSystemService().addFile(File("path1/devenv.exe"), VirtualFileSystemService.Attributes()))

        // When
        every { _visualStudioProvider1.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.VisualStudio, File("pathAbc"), Version.parse("1.2.03"), Version.parse("2019"), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudio, File("path1"), Version.parse("1.2.03"), Version.parse("2019"), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudioTest, File("path3"), Version.parse("1.4"), Version.parse("2017"), Platform.Default)
        )
        every { _visualStudioProvider2.getInstances() } returns listOf(
                ToolInstance(ToolInstanceType.MSTest, File("path3"), Version.parse("1.4"), Version.parse("2017"), Platform.Default),
                ToolInstance(ToolInstanceType.VisualStudio, File("path2"), Version.parse("1.2.03.4"), Version.parse("2003"), Platform.Default)
        )

        // Then
        Assert.assertEquals(propertiesProvider.desription, "Visual Studio")
        Assert.assertEquals(
                propertiesProvider.properties.toList(),
                listOf(
                        AgentProperty(ToolInstanceType.VisualStudio, "VS2019", "1.2.03"),
                        AgentProperty(ToolInstanceType.VisualStudio, "VS2019_Path", "path1")
                )
        )
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            VisualStudioAgentPropertiesProvider(listOf(_visualStudioProvider1, _visualStudioProvider2), fileSystemService)
}