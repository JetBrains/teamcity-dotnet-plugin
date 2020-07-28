package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.TargetingPackFileSystemAgentPropertiesProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TargetingPackFileSystemAgentPropertiesProviderTest {
    @MockK private lateinit var _environment: Environment
    private val _root86 = File("root86")
    private val _root64 = File("root64")
    private val _base86 = File(File(File(_root86, "Reference Assemblies"), "Microsoft"), "Framework")
    private val _base64 = File(File(File(_root64, "Reference Assemblies"), "Microsoft"), "Framework")
    private val _v35_86 = File(File(File(_base86, "v3.5"), "RedistList"), "FrameworkList.xml")
    private val _v471_86 = File(File(File(File(_base86, ".NETFramework"), "v4.7.1"), "RedistList"), "FrameworkList.xml")
    private val _v471_64 = File(File(File(File(_base64, ".NETFramework"), "v4.7.1"), "RedistList"), "FrameworkList.xml")
    private val _v4X_86 = File(File(File(File(_base86, ".NETFramework"), "v4.X"), "RedistList"), "FrameworkList.xml")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _environment.tryGetVariable("ProgramFiles(x86)") } returns _root86.path
        every { _environment.tryGetVariable("ProgramW6432") } returns _root64.path
    }

    @DataProvider
    fun testProperties(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(_v35_86),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack3.5_Path", File(_base86, "v3.5").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(_v471_64),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack4.7.1_Path", File(File(_base64, ".NETFramework"), "v4.7.1").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(_v471_86)
                                .addFile(_v471_64),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack4.7.1_Path", File(File(_base86, ".NETFramework"), "v4.7.1").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(_v4X_86),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack4.X_Path", File(File(_base86, ".NETFramework"), "v4.X").path)
                        )
                )
                ,
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(_v35_86)
                                .addFile(_v471_86)
                                .addFile(_v471_64)
                                .addFile(_v4X_86),
                        listOf(
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack3.5_Path", File(_base86, "v3.5").path),
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack4.7.1_Path", File(File(_base86, ".NETFramework"), "v4.7.1").path),
                                AgentProperty(ToolInstanceType.TargetingPack, "DotNetFrameworkTargetingPack4.X_Path", File(File(_base86, ".NETFramework"), "v4.X").path)
                        )
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(File(File(File(_base64, ".NETFramework"), "v4.7.2"), "RedistList"), "FrameworkList.xml")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File(File(_base64, ".NETFramework"), "4.7.2"), "RedistList"), "FrameworkList.xml")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File(File(_base64, ".NETFramework"), "v4.7.2sss"), "RedistList"), "FrameworkList.xml")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File(File(_base64, ".NETFramework"), " "), "RedistList"), "FrameworkList.xml")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(File(_base64, ".NETFramework"), "v4.7.2"), "RedistList")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(File(File(_base64, ".NETFramework"), "v4.7.2"), "RedistList")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(File(_base64, ".NETFramework"), "v4.7.2")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(File(_base64, ".NETFramework"), "v4.7.2")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(_base64, ".NETFramework")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(File(_base64, ".NETFramework")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File(_base64, ".NETFramework")),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(_base64),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(_base64),
                        emptyList<AgentProperty>()
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        emptyList<AgentProperty>()
                )
        )
    }

    @Test(dataProvider = "testProperties")
    fun shouldProvideProperties(
            fileSystemService: FileSystemService,
            expectedProperties: List<AgentProperty>) {
        // Given
        val propertiesProvider = createInstance(fileSystemService)

        // When
        val actualProperties = propertiesProvider.properties.toList()

        // Then
        Assert.assertEquals(actualProperties, expectedProperties)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            TargetingPackFileSystemAgentPropertiesProvider(_environment, fileSystemService)
}