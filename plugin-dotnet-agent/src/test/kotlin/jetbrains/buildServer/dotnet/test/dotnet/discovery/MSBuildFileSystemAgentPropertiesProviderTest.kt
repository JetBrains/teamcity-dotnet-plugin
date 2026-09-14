package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.dotnet.discovery.MSBuildFileSystemAgentPropertiesProvider
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
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/arm64/MSBuild.exe")),
                        listOf(
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default),
                                ToolInstance(ToolInstanceType.VisualStudio, File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/Common7/IDE"), Version.Empty, Version.Empty, Platform.Default)
                        ),
                        listOf(
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path),
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path),
                                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools16.0_ARM64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/arm64").path)
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

    @Test
    fun shouldNotPublishPropertyWhenMSBuildVersionCannotBeRead() {
        // Given
        val msBuildPath = File("Program Files (x86)/Microsoft Visual Studio/2026/BuildTools/MSBuild/Current/Bin/MSBuild.exe")
        val propertiesProvider = createInstance(VirtualFileSystemService().addFile(msBuildPath))
        every { _visualStudioLocator.getInstances() } returns listOf(
            ToolInstance(
                ToolInstanceType.VisualStudio,
                File("Program Files (x86)/Microsoft Visual Studio/2026/BuildTools/Common7/IDE"),
                Version(18, 10, 1),
                Version(2026),
                Platform.Default,
            )
        )
        every { _peReader.tryGetVersion(msBuildPath) } returns Version.Empty
        every { _peReader.tryGetFileVersion(msBuildPath) } returns Version.Empty

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, emptyList<AgentProperty>())
    }

    @Test
    fun shouldUseFileVersionWhenProductVersionCannotBeRead() {
        // Given
        val basePath = File("Program Files (x86)/Microsoft Visual Studio/2026/BuildTools")
        val msBuildX86Path = File(basePath, "MSBuild/Current/Bin/MSBuild.exe")
        val msBuildX64Path = File(basePath, "MSBuild/Current/Bin/amd64/MSBuild.exe")
        val msBuildArm64Path = File(basePath, "MSBuild/Current/Bin/arm64/MSBuild.exe")
        val propertiesProvider = createInstance(
            VirtualFileSystemService()
                .addFile(msBuildX86Path)
                .addFile(msBuildX64Path)
                .addFile(msBuildArm64Path)
        )
        every { _visualStudioLocator.getInstances() } returns listOf(
            ToolInstance(
                ToolInstanceType.VisualStudio,
                File(basePath, "Common7/IDE"),
                Version(18, 10, 1),
                Version(2026),
                Platform.Default,
            )
        )
        every { _peReader.tryGetVersion(any()) } returns Version.Empty
        every { _peReader.tryGetFileVersion(any()) } returns Version(18, 10, 1, 42706)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(
            actualProperties,
            listOf(
                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools18.0_x86_Path", msBuildX86Path.parent),
                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools18.0_x64_Path", msBuildX64Path.parent),
                AgentProperty(ToolInstanceType.MSBuildTool, "MSBuildTools18.0_ARM64_Path", msBuildArm64Path.parent),
            )
        )
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            MSBuildFileSystemAgentPropertiesProvider(listOf(_visualStudioLocator), fileSystemService, _peReader)
}
