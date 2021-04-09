package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.MSBuildFileSystemAgentPropertiesProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildFileSystemAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioLocator: ToolInstanceProvider
    @MockK private lateinit var _peReader: PEReader

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testProperties(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path),
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild22.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/1abc6.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        emptyList<ToolInstance>(),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.MSTest, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudioTest, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        emptyList<AgentProperty>()
                )
        )
    }

    @Test(dataProvider = "testProperties")
    fun shouldProvideProperties(
            fileSystemService: FileSystemService,
            visualStudioInstances: List<ToolInstance>,
            expectedProperties: List<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance(fileSystemService)
        every { _visualStudioLocator.getInstances() } returns visualStudioInstances
        every { _peReader.tryGetVersion(any()) } returns Version(16, 0, 0, 0)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            MSBuildFileSystemAgentPropertiesProvider(listOf(_visualStudioLocator), fileSystemService, _peReader)
}