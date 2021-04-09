package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetSetupRegistryVisitorTest {
    @MockK private lateinit var _environment: DotnetFrameworksEnvironment
    private val _key = DotnetSetupRegistryVisitor.Keys.first()
    private val _root = File("root")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // v3.0 & v3.5
                // Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\NET Framework Setup\NDP\v3.5
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 0, 30729, 4926), File(_root, "v3.0"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "abc")
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "")
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        null,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Int, 3L)
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "V3.0" + "VersioN", WindowsRegistryValueType.Str, "3.0.30729.4926")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 0, 30729, 4926), File(_root, "v3.0"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926), File(_root, "v3.5"))
                        )
                ),
                arrayOf(
                        null,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "InstallPATH", WindowsRegistryValueType.Str, "abc")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926),  File(_root, "v3.5"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "   ")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926),  File(_root, "v3.5"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "Version", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Int, 1L)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926),  File(_root, "v3.5"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.5" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "V3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v3.0" + "Version", WindowsRegistryValueType.Str, "3.0.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "versioN", WindowsRegistryValueType.Str, "3.5.30729.4926"),
                                WindowsRegistryValue(_key + "v3.5" + "InstallPATH", WindowsRegistryValueType.Str, "abc")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(3, 0, 30729, 4926), File(_root, "v3.0")),
                                DotnetFramework(_key.bitness.platform, Version(3, 5, 30729, 4926), File("abc"))
                        )
                ),
                // v4
                // Computer\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\NET Framework Setup\NDP\v4\Full
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version.parse("4.8.04084"), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(4, 8), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(4, 8), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Str, "528040")
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "FULL" + "VersioN", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "V4" + "Full" + "InstallPatH", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version.parse("4.8.04084"), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, ""),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "  "),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff4)
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, " "),
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(4, 8), File("abc"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Full" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
                        ),
                        sequenceOf<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v4" + "Version", WindowsRegistryValueType.Str, "4.8.04084"),
                                WindowsRegistryValue(_key + "v4" + "Full" + "InstallPath", WindowsRegistryValueType.Str, "abc"),
                                WindowsRegistryValue(_key + "v4" + "Release", WindowsRegistryValueType.Int, 0x80ff3)
                        ),
                        sequenceOf<DotnetFramework>()
                )
        )
    }

    @Test(dataProvider = "testData")
    fun shouldProvideFrameworks(root: File?, values: Sequence<WindowsRegistryValue>, expectedFrameworks: Sequence<DotnetFramework>) {
        // Given
        val visitor = createInstance()
        every {_environment.tryGetRoot(_key.bitness)} returns root

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
        val visitor = createInstance()
        every {_environment.tryGetRoot(_key.bitness)} returns _root
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
        val visitor = createInstance()

        // When
        val actualKeys = visitor.keys.toList()

        // Then
        Assert.assertEquals(actualKeys.size, 2)
    }

    private fun createInstance() =
            DotnetSetupRegistryVisitor(_environment)
}