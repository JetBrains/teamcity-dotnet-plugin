package jetbrains.buildServer.dotnet.test.visualStudio

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.dotnet.Platform
import jetbrains.buildServer.visualStudio.VisualStudioInstanceParser
import jetbrains.buildServer.visualStudio.VisualStudioFileSystemProvider
import jetbrains.buildServer.visualStudio.VisualStudioPackagesLocator
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.io.InputStream
import java.io.PipedInputStream

class VisualStudioFileSystemProviderTest {
    @MockK private lateinit var _visualStudioPackagesLocator1: VisualStudioPackagesLocator
    @MockK private lateinit var _visualStudioPackagesLocator2: VisualStudioPackagesLocator
    @MockK private lateinit var _visualStudioInstancesParser: VisualStudioInstanceParser
    private val _inputStream1: InputStream = PipedInputStream()
    private val _inputStream2: InputStream = PipedInputStream()
    private val _visualStudioInstance1: ToolInstance = ToolInstance(ToolInstanceType.VisualStudio, File("a1"), Version.Empty, Version.Empty, Platform.Default)
    private val _visualStudioInstance2: ToolInstance = ToolInstance(ToolInstanceType.VisualStudio, File("a2"), Version.Empty, Version.Empty, Platform.Default)

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testInstances(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1, _visualStudioInstance2)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1, _visualStudioInstance2)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addDirectory(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1, _visualStudioInstance2)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1, _visualStudioInstance2)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        null,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state2.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addDirectory(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd"))
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_InstancesAbc/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances"))
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2"))
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages3/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        null,
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        listOf(_visualStudioInstance1)
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        emptyList<ToolInstance>()
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        null,
                        null,
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        emptyList<ToolInstance>()
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        null,
                        null,
                        _visualStudioInstance1,
                        _visualStudioInstance2,
                        emptyList<ToolInstance>()
                ),
                arrayOf(
                        VirtualFileSystemService()
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages1/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream1)
                                .addFile(File("ProgramData/Microsoft/VisualStudio/Packages2/_Instances/abcd/state.json"), VirtualFileSystemService.Attributes(), _inputStream2)
                                .addFile(File("a1/devenv.exe"), VirtualFileSystemService.Attributes())
                                .addFile(File("a2/devenv.exe"), VirtualFileSystemService.Attributes()),
                        "ProgramData/Microsoft/VisualStudio/Packages1",
                        "ProgramData/Microsoft/VisualStudio/Packages2",
                        _visualStudioInstance1,
                        null,
                        listOf(_visualStudioInstance1)
                )
        )
    }

    @Test(dataProvider = "testInstances")
    fun shouldProvideInstances(
            fileSystemService: FileSystemService,
            packagesPath1: String?,
            packagesPath2: String?,
            visualStudioInstance1: ToolInstance?,
            visualStudioInstance2: ToolInstance?,
            expectedInstances: List<ToolInstance>) {
        // Given
        val locator = createInstance(fileSystemService)
        every { _visualStudioPackagesLocator1.tryGetPackagesPath() } returns packagesPath1
        every { _visualStudioPackagesLocator2.tryGetPackagesPath() } returns packagesPath2
        every { _visualStudioInstancesParser.tryParse(_inputStream1) } returns visualStudioInstance1
        every { _visualStudioInstancesParser.tryParse(_inputStream2) } returns visualStudioInstance2

        // When
        val actualInstances = locator.getInstances()

        // Then
        Assert.assertEquals(actualInstances, expectedInstances)
    }

    private fun createInstance(fileSystemService: FileSystemService) =
            VisualStudioFileSystemProvider(listOf(_visualStudioPackagesLocator1, _visualStudioPackagesLocator2), fileSystemService, _visualStudioInstancesParser)
}