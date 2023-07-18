package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.DotnetWorkload
import jetbrains.buildServer.dotnet.DotnetWorkloadProvider
import jetbrains.buildServer.dotnet.FileBasedDotnetWorkloadProvider
import jetbrains.buildServer.dotnet.VersionEnumerator
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class FileBasedDotnetWorkloadProviderTest {

    @MockK
    private lateinit var _sdksProvider: DotnetSdksProvider

    @MockK
    private lateinit var _versionEnumerator: VersionEnumerator

    private val _dotnetExecutable = File("sdk", "dotnet")

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
                    .addFile(File("sdk/metadata/workloads/6.0.100/InstalledWorkloads/android"))
                    .addFile(File("sdk/metadata/workloads/7.1.100/InstalledWorkloads/android"))
                    .addFile(File("sdk/metadata/workloads/8.0.100-preview.4/InstalledWorkloads/android"))
                    .addFile(File("sdk/metadata/workloads/6.0.100/InstalledWorkloads/ios")),
                listOf("6.0.101", "6.0.102", "7.1.100", "8.0.100-preview.4.23260.5", "8.0.100"),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0")),
                    DotnetWorkload("android", Version.parse("6.0.101")),
                    DotnetWorkload("android", Version.parse("6.0.102")),
                    DotnetWorkload("android", Version.parse("7.1")),
                    DotnetWorkload("android", Version.parse("7.1.100")),
                    DotnetWorkload("android", Version.parse("8.0.100-preview.4.23260.5")),
                    DotnetWorkload("ios", Version.parse("6.0")),
                    DotnetWorkload("ios", Version.parse("6.0.101")),
                    DotnetWorkload("ios", Version.parse("6.0.102")),
                )
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("sdk/metadata/workloads/6.0.100/InstalledWorkloads/android"))
                    .addFile(File("sdk/metadata/workloads/6.0.200/InstalledWorkloads/ios"))
                    .addFile(File("sdk/metadata/workloads/6.0.300/InstalledWorkloads/wasm-tools")),
                listOf("6.0.102", "6.0.204", "6.0.306"),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.102")),
                    DotnetWorkload("ios", Version.parse("6.0.204")),
                    DotnetWorkload("wasm-tools", Version.parse("6.0")),
                    DotnetWorkload("wasm-tools", Version.parse("6.0.306")),
                )
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("sdk/metadata/workloads/6.0.100/InstalledWorkloads/android")),
                emptyList<String>(),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                )
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("sdk/metadata/workloads/6.0.100/InstalledWorkloads/androidDir/file")),
                emptyList<String>(),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("sdk/metadata/workloads/invalidVersion/InstalledWorkloads/android")),
                emptyList<String>(),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService()
                    .addFile(File("sdk/metadata/workloads/InstalledPacks/InstalledWorkloads/android")),
                emptyList<String>(),
                emptyList<DotnetWorkload>()
            ),
            arrayOf(
                VirtualFileSystemService(),
                emptyList<String>(),
                emptyList<DotnetWorkload>()
            ),
        )
    }

    @Test(dataProvider = "workloadsTestData")
    fun `should get installed workloads`(
        fileSystemService: FileSystemService,
        sdks: List<String>,
        expectedDotnetWorkloads: Collection<DotnetWorkload>
    ) {
        // given
        val workloadProvider = createInstance(fileSystemService)

        val sdksMock = mockk<Sequence<DotnetSdk>>()
        every { _sdksProvider.getSdks(_dotnetExecutable) } returns sdksMock
        every { _versionEnumerator.enumerate(sdksMock) } returns enumerateVersions(sdks)

        // when
        val actualResult = workloadProvider.getInstalledWorkloads(_dotnetExecutable)

        // then
        assertEquals(actualResult, expectedDotnetWorkloads)
    }

    private fun enumerateVersions(sdks: List<String>) = sequence {
        sdks
            .map { Version.parse(it) }
            .groupBy { Version(it.major, it.minor) }
            .forEach { (version, group) ->
                val maxVersion = group.maxByOrNull { it }!!
                yield("${version.major}.${version.minor}" to DotnetSdk(_dotnetExecutable, maxVersion))
                yieldAll(group.map { it.toString() to DotnetSdk(_dotnetExecutable, it) })
            }
    }.distinctBy { it.first }

    private fun createInstance(fileSystemService: FileSystemService): DotnetWorkloadProvider =
        FileBasedDotnetWorkloadProvider(fileSystemService, _sdksProvider, _versionEnumerator)
}