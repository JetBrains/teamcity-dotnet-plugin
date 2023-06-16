package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetWorkload
import jetbrains.buildServer.dotnet.DotnetWorkloadProvider
import jetbrains.buildServer.dotnet.FileBasedDotnetWorkloadProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FileBasedDotnetWorkloadProviderTest {
    private val _dotnetExecutable = File("dotnet")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun workloadsTestData(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("metadata/workloads/6.0.100/InstalledWorkloads/android"))
                    .addFile(File("metadata/workloads/7.0.100/InstalledWorkloads/android"))
                    .addFile(File("metadata/workloads/8.0.100/InstalledWorkloads/android"))
                    .addFile(File("metadata/workloads/6.0.100/InstalledWorkloads/ios")),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                    DotnetWorkload("android", Version.parse("7.0.100")),
                    DotnetWorkload("android", Version.parse("8.0.100")),
                    DotnetWorkload("ios", Version.parse("6.0.100")),
                )
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("metadata/workloads/6.0.100/InstalledWorkloads/androidDir/file")),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("metadata/workloads/invalidVersion/InstalledWorkloads/android")),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("metadata/workloads/InstalledPacks/InstalledWorkloads/android")),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService(),
                emptyList<DotnetWorkload>()
            ),
        )
    }

    @Test(dataProvider = "workloadsTestData")
    fun shouldGetInstalledWorkloads(
        fileSystemService: FileSystemService,
        expectedDotnetWorkloads: Collection<DotnetWorkload>
    ) {
        // Given
        val workloadProvider = createInstance(fileSystemService)

        // When
        val actualResult = workloadProvider.getInstalledWorkloads(_dotnetExecutable)

        // Then
        assertEquals(actualResult, expectedDotnetWorkloads)
    }

    private fun createInstance(fileSystemService: FileSystemService): DotnetWorkloadProvider =
        FileBasedDotnetWorkloadProvider(fileSystemService)
}