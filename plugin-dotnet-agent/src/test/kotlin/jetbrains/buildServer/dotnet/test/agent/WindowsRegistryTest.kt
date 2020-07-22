package jetbrains.buildServer.dotnet.test.agent

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.WindowsRegistryParser
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class WindowsRegistryTest {
    @MockK lateinit var _environment: Environment
    @MockK lateinit var _commandLineExecutor: CommandLineExecutor
    @MockK lateinit var _windowsRegistryParser: WindowsRegistryParser
    private val _rootKey = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.CURRENT_USER, "myKey")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testGetData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        OSType.WINDOWS,
                        listOf(Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""))),
                        listOf(WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""))),
                arrayOf(
                        OSType.WINDOWS,
                        listOf(
                                Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey, "2", _rootKey + "subKey1")
                        ),
                        listOf(
                                WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey1"
                        )
                ),
                arrayOf(
                        OSType.WINDOWS,
                        listOf(
                                Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey, "2", _rootKey + "subKey1"),
                                Item(_rootKey + "subKey1", "3", WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, ""))
                        ),
                        listOf(
                                WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey1",
                                WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, "")
                        )
                ),
                arrayOf(
                        OSType.WINDOWS,
                        listOf(
                                Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey, "2", _rootKey + "subKey1"),
                                Item(_rootKey + "subKey1", "3", WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, "")),
                                Item(_rootKey + "subKey1", "4", null),
                                Item(_rootKey, "4", _rootKey + "subKey2")
                        ),
                        listOf(
                                WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey1",
                                WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey2"
                        )
                ),
                arrayOf(
                        OSType.WINDOWS,
                        listOf(
                                Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey, "2", _rootKey + "subKey1"),
                                Item(_rootKey + "subKey1", "3", WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, "")),
                                Item(_rootKey + "subKey1", "4", null),
                                Item(_rootKey, "4", _rootKey + "subKey2"),
                                Item(_rootKey + "subKey2", "5", WindowsRegistryValue(_rootKey  + "subKey2", WindowsRegistryValueType.Str, ""))
                        ),
                        listOf(
                                WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey1",
                                WindowsRegistryValue(_rootKey  + "subKey1", WindowsRegistryValueType.Str, ""),
                                _rootKey + "subKey2",
                                WindowsRegistryValue(_rootKey  + "subKey2", WindowsRegistryValueType.Str, "")
                        )
                ),
                arrayOf(
                        OSType.UNIX,
                        listOf(Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""))),
                        emptyList<Any>()),
                arrayOf(
                        OSType.MAC,
                        listOf(Item(_rootKey, "1", WindowsRegistryValue(_rootKey, WindowsRegistryValueType.Str, ""))),
                        emptyList<Any>()))
    }

    @Test(dataProvider = "testGetData")
    fun shouldGet(os: OSType, items: List<Item>, expectedItems: List<Any>) {
        // Given
        val registry = createInstance()
        val lines = items.map { it.line }.distinct().toList()

        // When
        every { _environment.os } returns os
        every { _commandLineExecutor.tryExecute(
                CommandLine(
                        null,
                        TargetType.SystemDiagnostics,
                        Path("REG"),
                        Path("."),
                        listOf(
                                CommandLineArgument("QUERY"),
                                CommandLineArgument(_rootKey.regKey),
                                CommandLineArgument("/reg:64"),
                                CommandLineArgument("/s")))) } returns CommandLineResult(0, lines, emptyList())

        for (item in items) {
            if (item.item is WindowsRegistryValue) {
                every { _windowsRegistryParser.tryParseValue(item.targetKey, item.line) } returns item.item as WindowsRegistryValue?
            }
            else {
                every { _windowsRegistryParser.tryParseValue(item.targetKey, item.line) } returns null
            }

            if (item.item is WindowsRegistryKey) {
                every { _windowsRegistryParser.tryParseKey(item.targetKey, item.line) } returns item.item as WindowsRegistryKey?
            }
            else {
                every { _windowsRegistryParser.tryParseKey(item.targetKey, item.line) } returns null
            }
        }

        val actualItems = mutableListOf<Any>()
        registry.get(
                _rootKey,
                object :WindowsRegistryVisitor {
                    override fun accept(key: WindowsRegistryKey): Boolean {
                        actualItems.add(key)
                        return true
                    }

                    override fun accept(value: WindowsRegistryValue): Boolean {
                        actualItems.add(value)
                        return true
                    }
                },
                true)

        // Then
        Assert.assertEquals(actualItems, expectedItems)
    }

    private fun createInstance(): WindowsRegistry = WindowsRegistryImpl(_environment, _commandLineExecutor, _windowsRegistryParser)

    data class Item(
            val targetKey: WindowsRegistryKey,
            val line: String,
            val item: Any?)
}