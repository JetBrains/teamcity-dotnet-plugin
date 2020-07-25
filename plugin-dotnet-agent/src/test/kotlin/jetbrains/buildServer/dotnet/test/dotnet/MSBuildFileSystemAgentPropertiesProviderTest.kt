package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.agent.runner.AgentPropertyType
import jetbrains.buildServer.dotnet.MSBuildFileSystemAgentPropertiesProvider
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.visualStudio.VisualStudioInstance
import jetbrains.buildServer.visualStudio.VisualStudioProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.util.PEReader.PEVersion
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildFileSystemAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioLocator: VisualStudioProvider
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
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path),
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild22.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/1abc6.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        listOf(
                                AgentProperty(AgentPropertyType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        listOf(
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty),
                                VisualStudioInstance(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty)
                        ),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        emptyList<VisualStudioInstance>(),
                        emptyList<AgentProperty>()
                )
        )
    }

    @Test(dataProvider = "testProperties")
    fun shouldProvideProperties(
            fileSystemService: FileSystemService,
            visualStudioInstances: List<VisualStudioInstance>,
            expectedProperties: List<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance(fileSystemService)
        every { _visualStudioLocator.getInstances() } returns visualStudioInstances.asSequence()
        every { _peReader.tryGetProductVersion(any()) } returns Version(16, 0, 0, 0)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            MSBuildFileSystemAgentPropertiesProvider(listOf(_visualStudioLocator), fileSystemService, _peReader)
}