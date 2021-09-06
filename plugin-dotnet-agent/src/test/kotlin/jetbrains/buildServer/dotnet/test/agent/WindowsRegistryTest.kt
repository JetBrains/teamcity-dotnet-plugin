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
    private val _rootKey32 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness32, WindowsRegistryHive.CURRENT_USER, "myKey32")
    private val _rootKey64 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.CURRENT_USER, "myKey64")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testGetData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows 10",
                        listOf(Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""))),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey64.regKey), CommandLineArgument("/reg:64"), CommandLineArgument("/s")),
                        listOf(WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""))),
                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows 10",
                        listOf(
                                Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64, "2", _rootKey64 + "subKey1")
                        ),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey64.regKey), CommandLineArgument("/reg:64"), CommandLineArgument("/s")),
                        listOf(
                                WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey1"
                        )
                ),
                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows 10",
                        listOf(
                                Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64, "2", _rootKey64 + "subKey1"),
                                Item(_rootKey64 + "subKey1", "3", WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, ""))
                        ),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey64.regKey), CommandLineArgument("/reg:64"), CommandLineArgument("/s")),
                        listOf(
                                WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey1",
                                WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, "")
                        )
                ),
                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows 10",
                        listOf(
                                Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64, "2", _rootKey64 + "subKey1"),
                                Item(_rootKey64 + "subKey1", "3", WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64 + "subKey1", "4", null),
                                Item(_rootKey64, "4", _rootKey64 + "subKey2")
                        ),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey64.regKey), CommandLineArgument("/reg:64"), CommandLineArgument("/s")),
                        listOf(
                                WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey1",
                                WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey2"
                        )
                ),
                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows 10",
                        listOf(
                                Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64, "2", _rootKey64 + "subKey1"),
                                Item(_rootKey64 + "subKey1", "3", WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, "")),
                                Item(_rootKey64 + "subKey1", "4", null),
                                Item(_rootKey64, "4", _rootKey64 + "subKey2"),
                                Item(_rootKey64 + "subKey2", "5", WindowsRegistryValue(_rootKey64  + "subKey2", WindowsRegistryValueType.Str, ""))
                        ),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey64.regKey), CommandLineArgument("/reg:64"), CommandLineArgument("/s")),
                        listOf(
                                WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey1",
                                WindowsRegistryValue(_rootKey64  + "subKey1", WindowsRegistryValueType.Str, ""),
                                _rootKey64 + "subKey2",
                                WindowsRegistryValue(_rootKey64  + "subKey2", WindowsRegistryValueType.Str, "")
                        )
                ),
                arrayOf(
                        _rootKey64,
                        OSType.UNIX,
                        "Linux",
                        listOf(Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""))),
                        emptyList<CommandLineArgument>(),
                        emptyList<Any>()),
                arrayOf(
                        _rootKey64,
                        OSType.MAC,
                        "Mac",
                        listOf(Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""))),
                        emptyList<CommandLineArgument>(),
                        emptyList<Any>()),

                // Windows XP
                arrayOf(
                        _rootKey32,
                        OSType.WINDOWS,
                        "Windows XP",
                        listOf(Item(_rootKey32, "1", WindowsRegistryValue(_rootKey32, WindowsRegistryValueType.Str, ""))),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey32.regKey), CommandLineArgument("/s")),
                        listOf(WindowsRegistryValue(_rootKey32, WindowsRegistryValueType.Str, ""))),

                arrayOf(
                        _rootKey64,
                        OSType.WINDOWS,
                        "Windows XP",
                        listOf(Item(_rootKey64, "1", WindowsRegistryValue(_rootKey64, WindowsRegistryValueType.Str, ""))),
                        emptyList<CommandLineArgument>(),
                        emptyList<Any>()),

                // First line is "! REG.EXE VERSION 3.0"
                arrayOf(
                        _rootKey32,
                        OSType.WINDOWS,
                        "Windows XP",
                        listOf(Item(_rootKey32, "! REG.EXE VERSION 3.0", null), Item(_rootKey32, "1", WindowsRegistryValue(_rootKey32, WindowsRegistryValueType.Str, ""))),
                        listOf(CommandLineArgument("QUERY"), CommandLineArgument(_rootKey32.regKey), CommandLineArgument("/s")),
                        listOf(WindowsRegistryValue(_rootKey32, WindowsRegistryValueType.Str, ""))),
        )
    }

    @Test(dataProvider = "testGetData")
    fun shouldGet(key: WindowsRegistryKey, os: OSType, osName: String, items: List<Item>, args: List<CommandLineArgument>, expectedItems: List<Any>) {
        // Given
        val registry = createInstance()
        val lines = items.map { it.line }.distinct().toList()

        // When
        every { _environment.os } returns os
        every { _environment.osName } returns osName
        every { _commandLineExecutor.tryExecute(
                CommandLine(
                        null,
                        TargetType.SystemDiagnostics,
                        Path("REG"),
                        Path("."),
                        args
                )
        ) } returns CommandLineResult(0, lines, emptyList())

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
        registry.accept(
                key,
                object :WindowsRegistryVisitor {
                    override fun visit(key: WindowsRegistryKey): Boolean {
                        actualItems.add(key)
                        return true
                    }

                    override fun visit(value: WindowsRegistryValue): Boolean {
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