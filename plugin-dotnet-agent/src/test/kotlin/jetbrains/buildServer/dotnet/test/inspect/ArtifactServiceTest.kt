package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.agent.inspections.InspectionReporter
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.inspect.ArtifactServiceImpl
import jetbrains.buildServer.inspect.InspectionTool
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.File

class ArtifactServiceTest {
    @MockK private lateinit var _artifactsWatcher: ArtifactsWatcher

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider(name = "publishCases")
    fun getPublishCasesCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        VirtualFileSystemService().addFile(File("Abc", "file.xml"), VirtualFileSystemService.Attributes(), ByteArrayInputStream(ByteArray(1))),
                        InspectionTool.Inspectcode,
                        File("Abc", "file.xml"),
                        Path("rt"),
                        "${File("Abc", "file.xml").canonicalPath}=>.teamcity/${InspectionTool.Inspectcode.runnerType}/rt"
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("Abc", "file.xml"), VirtualFileSystemService.Attributes(), ByteArrayInputStream(ByteArray(1))),
                        InspectionTool.Inspectcode,
                        File("Abc", "file.xml"),
                        null,
                        "${File("Abc", "file.xml").canonicalPath}=>.teamcity/${InspectionTool.Inspectcode.runnerType}/"
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("Abc", "file.xml"), VirtualFileSystemService.Attributes(), ByteArrayInputStream(ByteArray(1))),
                        InspectionTool.Dupfinder,
                        File("Abc", "file.xml"),
                        Path("rt"),
                        "${File("Abc", "file.xml").canonicalPath}=>.teamcity/${InspectionTool.Dupfinder.runnerType}/rt"
                ),
                arrayOf(
                        VirtualFileSystemService().addFile(File("Abc", "file.xml")),
                        InspectionTool.Inspectcode,
                        File("Abc", "file.xml"),
                        Path("rt"),
                        null
                ),
                arrayOf(
                        VirtualFileSystemService().addDirectory(File("Abc", "file.xml")),
                        InspectionTool.Inspectcode,
                        File("Abc", "file.xml"),
                        Path("rt"),
                        null
                ),
                arrayOf(
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        File("Abc", "file.xml"),
                        Path("rt"),
                        null
                )
        )
    }

    @Test(dataProvider = "publishCases")
    fun shouldPublish(
            fileSystem: FileSystemService,
            tool: InspectionTool,
            artifactSource: File,
            artifactDestination: Path?,
            expectedNewArtifactsPath: String?) {
        // Given
        val artifactService = createInstance(fileSystem)
        every { _artifactsWatcher.addNewArtifactsPath(any()) } returns Unit

        // When
        artifactService.publish(tool, artifactSource, artifactDestination)

        // Then
        expectedNewArtifactsPath?.let {
            verify { _artifactsWatcher.addNewArtifactsPath(it) }
        }
    }

    private fun createInstance(fileSystem: FileSystemService) =
            ArtifactServiceImpl(
                    fileSystem,
                    _artifactsWatcher)
}