package jetbrains.buildServer.dotnet.test.dotnet.discovery

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFramework
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFrameworksEnvironment
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetSetupRegistryVisitor
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetSetupRegistryVisitorTest {
    private val _key = DotnetSetupRegistryVisitor.Keys.first { it.bitness == WindowsRegistryBitness.Bitness64 }
    private val _dotnetFrameworkRoot = File("Framework")
    private val _dotnetFrameworkArm64Root = File("FrameworkArm64")
    @MockK private lateinit var _envWithDotnetFramework: DotnetFrameworksEnvironment
    @MockK private lateinit var _envWithArmDotnetFramework: DotnetFrameworksEnvironment
    @MockK private lateinit var _envWithoutFrameworks: DotnetFrameworksEnvironment

    @BeforeClass
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        _envWithDotnetFramework.setupEnvironmentMock(frameworkRoot = _dotnetFrameworkRoot)
        _envWithArmDotnetFramework.setupEnvironmentMock(frameworkRoot = _dotnetFrameworkRoot, frameworkArm64Root = _dotnetFrameworkArm64Root)
        _envWithoutFrameworks.setupEnvironmentMock()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> = arrayOf(
        // v3.0 & v3.5
        // Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\NET Framework Setup\NDP\v3.5
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 0, 30729, 4926), File(_dotnetFrameworkRoot, "v3.0"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "abc")
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "")
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithoutFrameworks,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
            ),
            emptySequence<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
            ),
            emptySequence<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Int, 3L)
            ),
            emptySequence<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "V3.0" + "VersioN", WindowsRegistryValueType.Str, "3.0.30729.4926")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 0, 30729, 4926), File(_dotnetFrameworkRoot, "v3.0"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File(_dotnetFrameworkRoot, "v3.5"))
            )
        ),
        arrayOf(
            _envWithoutFrameworks,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926")
            ),
            emptySequence<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "InstallPATH", WindowsRegistryValueType.Str, "abc")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File(_dotnetFrameworkRoot, "v3.5"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "   ")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File(_dotnetFrameworkRoot, "v3.5"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Int, 1L)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File(_dotnetFrameworkRoot, "v3.5"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "V3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                WindowsRegistryValue(_key + "v3.5" + "InstallPATH", WindowsRegistryValueType.Str, "abc")
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 0, 30729, 4926), File(_dotnetFrameworkRoot, "v3.0")),
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(3, 5, 30729, 4926), File("abc"))
            )
        ),
        // v4
        // Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\NET Framework Setup\NDP\v4\Full
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version.parse("4.8.04084"), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Str, "528040")
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "FULL" + "VersioN", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "V4" + "Full" + "InstallPatH", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version.parse("4.8.04084"), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, ""),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "  "),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, " "),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.09037"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x8234d)
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8, 1), File("abc"))
            )
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
            ),
            sequenceOf<DotnetFramework>()
        ),
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                WindowsRegistryValue(_key + "v4" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
            ),
            sequenceOf<DotnetFramework>()
        ),
        // 4.8.1 Framework with ARM64 support
        arrayOf(
            _envWithArmDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.09032"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, File("Framework", "v4.0.30319").toString()),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 533320) // 4.8.1
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = true), Version(4, 8, 1), File("FrameworkArm64", "v4.0.30319")),
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8, 1), File("Framework", "v4.0.30319"))
            )
        ),
        // 4.8.1 Framework without ARM64 support
        arrayOf(
            _envWithDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.09032"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, File("Framework", "v4.0.30319").toString()),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 533320) // 4.8.1
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8, 1), File("Framework", "v4.0.30319"))
            )
        ),
        // 4.8.0 should not check for ARM support
        arrayOf(
            _envWithArmDotnetFramework,
            sequenceOf(
                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.0"),
                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, File("Framework", "v4.0.30319").toString()),
                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 528040) // 4.8.0
            ),
            sequenceOf(
                DotnetFramework(_key.bitness.getPlatform(isArm = false), Version(4, 8, 0), File("Framework", "v4.0.30319"))
            )
        )
    )

    @Test(dataProvider = "testData")
    fun shouldProvideFrameworks(
        environment: DotnetFrameworksEnvironment,
        values: Sequence<WindowsRegistryValue>,
        expectedFrameworks: Sequence<DotnetFramework>
    ) {
        // Given
        val visitor = createInstance(environment)

        // When
        val accepted = values.fold(true) {
            acc, value -> acc && visitor.visit(value)
        }

        val actualFrameworks = visitor.getFrameworks().sortedBy { it.toString() }.toList()

        // Then
        Assert.assertTrue(accepted)
        Assert.assertEquals(actualFrameworks, expectedFrameworks.sortedBy { it.toString() }.toList())
    }

    @Test
    fun shouldClearFrameworksAfterGet() {
        // Given
        val visitor = createInstance(_envWithDotnetFramework)
        visitor.visit(WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926"))

        // When
        visitor.getFrameworks().toList()
        val actualFrameworks = visitor.getFrameworks().toList()

        // Then
        Assert.assertEquals(actualFrameworks, emptyList<DotnetFramework>())
    }

    @Test
    fun shouldProvide2Keys() {
        // Given
        val visitor = createInstance(_envWithDotnetFramework)

        // When
        val actualKeys = visitor.keys.toList()

        // Then
        Assert.assertEquals(actualKeys.size, 2)
    }

    private fun DotnetFrameworksEnvironment.setupEnvironmentMock(frameworkRoot: File? = null, frameworkArm64Root: File? = null) {
        every { tryGetRoot(any()) } returns frameworkRoot
        every { tryGetRoot(any(), isArm = false) } returns frameworkRoot
        every { tryGetRoot(any(), isArm = true) } returns frameworkArm64Root
    }

    private fun createInstance(environment: DotnetFrameworksEnvironment) =
        DotnetSetupRegistryVisitor(environment)
}