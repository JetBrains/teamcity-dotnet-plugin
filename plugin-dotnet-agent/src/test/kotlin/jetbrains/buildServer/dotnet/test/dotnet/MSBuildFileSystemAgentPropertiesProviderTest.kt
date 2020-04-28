package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.PEReader
import jetbrains.buildServer.dotnet.MSBuildFileSystemAgentPropertiesProvider
import jetbrains.buildServer.dotnet.VisualStudioInstance
import jetbrains.buildServer.dotnet.VisualStudioLocator
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.util.PEReader.PEVersion
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class MSBuildFileSystemAgentPropertiesProviderTest {
    @MockK private lateinit var _visualStudioLocator: VisualStudioLocator
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
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/msbuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path),
                                AgentProperty("MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild22.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin/MSBuild.exe"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/1abc6.0/Bin/amd64/msbuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x86_Path", File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild/Current/Bin").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addDirectory(File("Program Files (x86)/Microsoft Visual Studio/2017/Professional/MSBuild"))
                                .addFile(File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64/MSBuild.exe")),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
                        ),
                        listOf(
                                AgentProperty("MSBuildTools16.0_x64_Path", File("Program Files (x86)/Microsoft Visual Studio/2019/Professional/MSBuild/16.0/Bin/amd64").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        listOf(
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2017/Professional", "", ""),
                                VisualStudioInstance("Program Files (x86)/Microsoft Visual Studio/2019/Professional", "", "")
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
        every { _visualStudioLocator.instances } returns visualStudioInstances.asSequence()
        every { _peReader.tryGetProductVersion(any()) } returns PEVersion(16, 0, 0, 0)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            MSBuildFileSystemAgentPropertiesProvider(_visualStudioLocator, fileSystemService, _peReader)
}