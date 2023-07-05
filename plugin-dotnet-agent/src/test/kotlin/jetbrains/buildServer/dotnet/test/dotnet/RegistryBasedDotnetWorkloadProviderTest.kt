package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.WindowsRegistryBitness.Bitness32
import jetbrains.buildServer.agent.WindowsRegistryBitness.Bitness64
import jetbrains.buildServer.agent.WindowsRegistryHive.LOCAL_MACHINE
import jetbrains.buildServer.dotnet.DotnetWorkload
import jetbrains.buildServer.dotnet.DotnetWorkloadProvider
import jetbrains.buildServer.dotnet.RegistryBasedDotnetWorkloadProvider
import jetbrains.buildServer.dotnet.VersionEnumerator
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdk
import jetbrains.buildServer.dotnet.discovery.dotnetSdk.DotnetSdksProvider
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.util.OSType.*
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RegistryBasedDotnetWorkloadProviderTest {

    @MockK
    private lateinit var _environment: Environment

    @MockK
    private lateinit var _windowsRegistry: WindowsRegistry

    @MockK
    private lateinit var _sdksProvider: DotnetSdksProvider

    @MockK
    private lateinit var _versionEnumerator: VersionEnumerator

    private val _dotnetExecutable = File("sdk", "dotnet")

    private val rootKey = WindowsRegistryKey.create(
        Bitness64, LOCAL_MACHINE,
        "SOFTWARE", "Microsoft", "dotnet", "InstalledWorkloads", "Standalone",
    )

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun `correct registry items to expected results`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(
                listOf(
                    rootKey + "x64" + "6.0.100" + "android",
                    rootKey + "x64" + "7.1.100" + "android",
                    rootKey + "x64" + "8.0.100-preview.4" + "android",
                    rootKey + "x64" + "6.0.100" + "ios",
                ),
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
                listOf(
                    rootKey + "x64" + "6.0.100" + "android",
                    rootKey + "x64" + "6.0.200" + "ios",
                    rootKey + "x64" + "6.0.300" + "wasm-tools",
                ),
                listOf("6.0.102", "6.0.204", "6.0.306"),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.102")),
                    DotnetWorkload("ios", Version.parse("6.0.204")),
                    DotnetWorkload("wasm-tools", Version.parse("6.0")),
                    DotnetWorkload("wasm-tools", Version.parse("6.0.306")),
                )
            ),
            arrayOf(
                listOf(
                    WindowsRegistryKey.create(
                        Bitness32, LOCAL_MACHINE,
                        "SOFTWARE", "Microsoft", "dotnet", "InstalledWorkloads", "Standalone",
                        "x86", "6.0.100", "android"
                    ),
                    rootKey + "x64" + "6.0.200" + "wasm-tools"
                ),
                emptyList<String>(),
                listOf(
                    DotnetWorkload("android", Version.parse("6.0.100")),
                    DotnetWorkload("wasm-tools", Version.parse("6.0.200"))
                )
            ),
        )
    }

    @Test(dataProvider = "correct registry items to expected results")
    fun `should fetch workloads from registry`(
        regKeys: List<WindowsRegistryKey>,
        sdks: List<String>,
        expectedDotnetWorkloads: Collection<DotnetWorkload>
    ) {
        // given
        val workloadProvider = createInstance()

        val sdksMock = mockk<Sequence<DotnetSdk>>()
        every { _environment.os } returns WINDOWS
        every { _sdksProvider.getSdks(_dotnetExecutable) } returns sdksMock
        every { _versionEnumerator.enumerate(sdksMock) } returns enumerateVersions(sdks)

        every { _windowsRegistry.accept(any(), any(), false) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (key in regKeys) {
                if (!visitor.visit(key))
                    break
            }
            value
        }

        // when
        val actualResult = workloadProvider.getInstalledWorkloads(_dotnetExecutable)

        // then
        assertEquals(actualResult, expectedDotnetWorkloads)
    }

    @DataProvider
    fun `wrong registry items`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(listOf(rootKey + "x64" + "6.0.100" + "android" + "extraKey")),
            arrayOf(listOf(rootKey + "x64" + "6.0.100" + "")),
            arrayOf(listOf(rootKey + "x64" + "6.0.100")),
            arrayOf(listOf(rootKey + "x64")),
            arrayOf(listOf(rootKey + "x64" + "wrongVersion" + "android")),
            arrayOf(listOf(rootKey + "wrongPlatform" + "6.0.100" + "android")),
            arrayOf(listOf(rootKey + "x65" + "6.0.100" + "android")),
            arrayOf(listOf(rootKey)),
            arrayOf(listOf(WindowsRegistryKey.create(Bitness64, LOCAL_MACHINE, "randomKey"))),
            arrayOf(listOf(WindowsRegistryKey.create(Bitness64, LOCAL_MACHINE))),
            arrayOf(listOf<WindowsRegistryKey>()),
        )
    }

    @Test(dataProvider = "wrong registry items")
    fun `should return empty result when registry items are not expected`(regKeys: List<WindowsRegistryKey>) {
        // given
        val workloadProvider = createInstance()

        val sdksMock = mockk<Sequence<DotnetSdk>>()
        every { _environment.os } returns WINDOWS
        every { _sdksProvider.getSdks(_dotnetExecutable) } returns sdksMock
        every { _versionEnumerator.enumerate(sdksMock) } returns emptySequence()

        every { _windowsRegistry.accept(any(), any(), false) } answers {
            val visitor = arg<WindowsRegistryVisitor>(1)
            for (key in regKeys) {
                if (!visitor.visit(key))
                    break
            }
            value
        }

        // when
        val actualResult = workloadProvider.getInstalledWorkloads(_dotnetExecutable)

        // then
        assertEquals(actualResult, emptyList<DotnetWorkload>())
    }

    @DataProvider
    fun notSupportedOs() = arrayOf(arrayOf(UNIX), arrayOf(MAC))

    @Test(dataProvider = "notSupportedOs")
    fun `should return empty collection when OS is not supported`(osType: OSType) {
        // given
        val workloadProvider = createInstance()

        every { _environment.os } returns osType

        // when
        val actualResult = workloadProvider.getInstalledWorkloads(_dotnetExecutable)

        // then
        assertEquals(actualResult, emptyList<DotnetWorkload>())
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

    private fun createInstance(): DotnetWorkloadProvider =
        RegistryBasedDotnetWorkloadProvider(
            _environment,
            _windowsRegistry,
            _sdksProvider,
            _versionEnumerator
        )
}