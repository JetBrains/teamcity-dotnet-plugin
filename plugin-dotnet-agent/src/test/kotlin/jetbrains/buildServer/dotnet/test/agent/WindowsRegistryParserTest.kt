package jetbrains.buildServer.dotnet.test.agent

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.WindowsRegistryParser
import jetbrains.buildServer.agent.WindowsRegistryParserImpl
import jetbrains.buildServer.agent.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class WindowsRegistryParserTest {
    @MockK private lateinit var _fileSystem: FileSystemService
    private val _key1 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.LOCAL_MACHINE, "SOFTWARE", "Microsoft", "MSBuild")
    private val _key2 = WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.CURRENT_USER, "myKey")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testDataKey(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\ToolsVersions", WindowsRegistryKey.create(WindowsRegistryBitness.Bitness64, WindowsRegistryHive.LOCAL_MACHINE, "SOFTWARE", "Microsoft", "MSBuild", "ToolsVersions")),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0\\abc", _key1 + "12.0" + "abc"),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0\\abc", _key1 + arrayOf("12.0", "abc")),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0", _key1 + "12.0"),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\xYz12.0", _key1 + "xYz12.0"),
                arrayOf("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0", _key1 + "12.0"),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\1", _key1 + "1"),
                arrayOf("    hkey_LOCAL_MACHINE\\SOFTWARE\\microsoft\\msbuild\\12.0", _key1 + "12.0"),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\", null),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild", null),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft", null),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\", null),
                arrayOf("abc    HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0", null),
                arrayOf("    abcHKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\MSBuild\\12.0", null),
                arrayOf("    HKEY_LOCAL_MACHINE\\SOFTWARE\\abc\\Microsoft\\MSBuild\\12.0", null),
                arrayOf("    abc  ", null),
                arrayOf("    ", null),
                arrayOf("", null)
        )
    }

    @Test(dataProvider = "testDataKey")
    fun shouldParseKey(text: String, expectedKey: WindowsRegistryKey?) {
        // Given
        val parser = createInstance()

        // When
        val actualValue = parser.tryParseKey(_key1, text)

        // Then
        Assert.assertEquals(actualValue, expectedKey)
    }

    @DataProvider
    fun testDataValue(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        "    MSBuildToolsPath\tREG_SZ\tc:\\WINDOWS\\Microsoft.NET\\Framework\\v2.0.50727\\",
                        WindowsRegistryValue(_key2 + "MSBuildToolsPath", WindowsRegistryValueType.Str, "c:\\WINDOWS\\Microsoft.NET\\Framework\\v2.0.50727\\"),
                        "c:\\WINDOWS\\Microsoft.NET\\Framework\\v2.0.50727\\",
                        0L),
                arrayOf(
                        "    aa bb    REG_SZ    abc bb",
                        WindowsRegistryValue(_key2 + "aa bb", WindowsRegistryValueType.Str, "abc bb"),
                        "abc bb",
                        0L),
                arrayOf(
                        "    zz    REG_SZ      zz  ",
                        WindowsRegistryValue(_key2 + "zz", WindowsRegistryValueType.Str, "  zz  "),
                        "  zz  ",
                        0L),
                arrayOf(
                        "    yy    REG_SZ    ",
                        WindowsRegistryValue(_key2 + "yy", WindowsRegistryValueType.Str, ""),
                        "",
                        0L),
                arrayOf(
                        "    yy    REG_SZ       ",
                        WindowsRegistryValue(_key2 + "yy", WindowsRegistryValueType.Str, "   "),
                        "   ",
                        0L),
                arrayOf(
                        "    bin    REG_BINARY    32473434739743243274892424",
                        WindowsRegistryValue(_key2 + "bin", WindowsRegistryValueType.Bin, "32473434739743243274892424"),
                        "",
                        0L),
                arrayOf(
                        "    dwordaa    REG_DWORD    0xf1",
                        WindowsRegistryValue(_key2 + "dwordaa", WindowsRegistryValueType.Int, "0xf1"),
                        "",
                        241L),
                arrayOf(
                        "    dwordaa    REG_DWORD    0x",
                        WindowsRegistryValue(_key2 + "dwordaa", WindowsRegistryValueType.Int, "0x"),
                        "",
                        0L),
                arrayOf(
                        "    dwordaa    REG_DWORD     ",
                        WindowsRegistryValue(_key2 + "dwordaa", WindowsRegistryValueType.Int, " "),
                        "",
                        0L),
                arrayOf(
                        "    dwordaa    REG_DWORD    0xaYYbc",
                        WindowsRegistryValue(_key2 + "dwordaa", WindowsRegistryValueType.Int, "0xaYYbc"),
                        "",
                        0L),
                arrayOf(
                        "    qwordaaa    REG_QWORD    0xFFaaBB100",
                        WindowsRegistryValue(_key2 + "qwordaaa", WindowsRegistryValueType.Long, "0xFFaaBB100"),
                        "",
                        68630065408L),
                arrayOf(
                        "    multstr    REG_MULTI_SZ    dedewdew\\0edewdw",
                        WindowsRegistryValue(_key2 + "multstr", WindowsRegistryValueType.Text, "dedewdew\\0edewdw"),
                        "dedewdew\nedewdw",
                        0L),
                arrayOf(
                        "    expstr    REG_EXPAND_SZ    dwedewdew ddewdewdewdewd",
                        WindowsRegistryValue(_key2 + "expstr", WindowsRegistryValueType.ExpandText, "dwedewdew ddewdewdewdewd"),
                        "dwedewdew ddewdewdewdewd",
                        0L),
                arrayOf(
                        "",
                        null,
                        "",
                        0),
                arrayOf(
                        "    abc    ",
                        null,
                        "",
                        0),
                arrayOf(
                        "    aa bb    REG_ABC    abc bb",
                        null,
                        "",
                        0)
                )
    }

    @Test(dataProvider = "testDataValue")
    fun shouldParseValue(
            text: String,
            expectedValue: WindowsRegistryValue?,
            expectedText: String,
            expectedNumber: Long) {
        // Given
        val parser = createInstance()

        // When
        val actualValue = parser.tryParseValue(_key2, text)

        // Then
        Assert.assertEquals(actualValue?.key, expectedValue?.key)
        Assert.assertEquals(actualValue?.type, expectedValue?.type)
        Assert.assertEquals(actualValue?.text ?: "", expectedText)
        Assert.assertEquals(actualValue?.number ?: 0L, expectedNumber)
    }

    private fun createInstance(): WindowsRegistryParser = WindowsRegistryParserImpl()
}