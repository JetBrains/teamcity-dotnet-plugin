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

class DotnetPolicyRegistryVisitorTest {
    @MockK private lateinit var _environment: DotnetFrameworksEnvironment
    private val _key = DotnetPolicyRegistryVisitor.Keys.first()
    private val _root = File("root")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(2, 0, 50727), File(_root, "v2.0.50727"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "050727", WindowsRegistryValueType.Str, "050727-50727")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version.parse("2.0.050727"), File(_root, "v2.0.050727"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727"),
                                WindowsRegistryValue(_key + "v4.0" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        sequenceOf(
                                DotnetFramework(_key.bitness.platform, Version(2, 0, 50727), File(_root, "v2.0.50727")),
                                DotnetFramework(_key.bitness.platform, Version(4, 0, 50727), File(_root, "v4.0.50727"))
                        )
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "abc", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Int, 50727L)
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Str, "")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        emptySequence<WindowsRegistryValue>(),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "abc", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        null,
                        sequenceOf(
                                WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
                ),
                arrayOf(
                        _root,
                        sequenceOf(
                                WindowsRegistryValue(WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.LOCAL_MACHINE, "SOFTWARE") + "v2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727")
                        ),
                        emptySequence<DotnetFramework>()
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
        visitor.visit(WindowsRegistryValue(_key + "v2.0" + "50727", WindowsRegistryValueType.Str, "50727-50727"))

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
            DotnetPolicyRegistryVisitor(_environment)
}