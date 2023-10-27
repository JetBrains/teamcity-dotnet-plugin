import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelectorImpl
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.test.StringExtensions.toPlatformPath
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.Assert.assertThrows
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverEntryPointSelectorImplTest {
    @MockK private lateinit var _tool: DotCoverAgentTool
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _selector: DotCoverEntryPointSelectorImpl

    @BeforeMethod
    fun setUp() {
        _tool = mockk(relaxed = true)
        _virtualContext = mockk(relaxed = true)
        _loggerService = mockk(relaxed = true)
        _selector = DotCoverEntryPointSelectorImpl(_tool, _virtualContext, _loggerService)
    }

    @DataProvider(name = "OS, entry point file name, tool type")
    fun `OS and entry point file name`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(OSType.WINDOWS, "dotCover.exe", DotCoverToolType.WindowsOnly),
            arrayOf(OSType.WINDOWS, "dotCover.exe", DotCoverToolType.DeprecatedCrossPlatform),
            arrayOf(OSType.UNIX, "dotCover.dll", DotCoverToolType.CrossPlatform),
            arrayOf(OSType.UNIX, "dotCover.sh", DotCoverToolType.DeprecatedCrossPlatform),
            arrayOf(OSType.MAC, "dotCover.dll", DotCoverToolType.CrossPlatform),
            arrayOf(OSType.MAC, "dotCover.sh", DotCoverToolType.DeprecatedCrossPlatform),
        )
    }

    @Test(dataProvider = "OS, entry point file name, tool type")
    fun `should return entry point and skip validation of agent parameters when build run in a container`(
        os: OSType, entryPointFileName: String, toolType: DotCoverToolType
    ) {
        // arrange
        val dotCoverHomePath = "/path/to/dotCover/home".toPlatformPath()
        val dotCoverEntryPointPath = "$dotCoverHomePath/$entryPointFileName".toPlatformPath()
        every { _tool.dotCoverHomePath } returns dotCoverHomePath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os
        every { _tool.type } returns toolType
        every { _tool.dotCoverExeFile } returns File(dotCoverEntryPointPath)
        every { _tool.dotCoverDllFile } returns File(dotCoverEntryPointPath)
        every { _tool.dotCoverShFile } returns File(dotCoverEntryPointPath)

        // act
        val result = _selector.select()

        // assert
        assertTrue(result.isSuccess)
        Assert.assertEquals(result.getOrNull()?.path, dotCoverEntryPointPath)
        verify (exactly = 1) { _tool.dotCoverHomePath }
        verify (exactly = 1) { _tool.type }
    }

    @DataProvider(name = "dotCover home directory")
    fun `dotCover home directory`(): Array<Array<Any?>> {
        return arrayOf(arrayOf(""), arrayOf(" "))
    }

    @Test(dataProvider = "dotCover home directory")
    fun `should return error when dotCover home path is invalid`(dotCoverPath: String) {
        // arrange
        every { _tool.dotCoverHomePath } returns dotCoverPath

        // act
        val result = _selector.select()

        // assert
        assertTrue(result.isFailure)
        assertThrows(ToolCannotBeFoundException::class.java) { result.getOrThrow() }
    }

    @DataProvider(name = "os type")
    fun `OS type`(): Array<Array<Any?>> {
        return arrayOf(arrayOf(OSType.WINDOWS), arrayOf(OSType.UNIX), arrayOf(OSType.MAC))
    }

    @Test(dataProvider = "os type")
    fun `should throw ToolCannotBeFoundException when dotCover tool type is Unknown`(os: OSType) {
        // arrange
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _tool.type } returns DotCoverToolType.Unknown

        // act
        val result = _selector.select()

        // assert
        assertTrue(result.isFailure)
        assertThrows(ToolCannotBeFoundException::class.java) { result.getOrThrow() }
    }

    @DataProvider(name = "non-Windows os type")
    fun `non-Windows os type`(): Array<Array<Any?>> {
        return arrayOf(arrayOf(OSType.UNIX), arrayOf(OSType.MAC))
    }

    @Test(dataProvider = "non-Windows os type")
    fun `should throw ToolCannotBeFoundException when dotCover tool is Windows and os is not Windows`(os: OSType) {
        // arrange
        every { _tool.dotCoverHomePath } returns "/path/to/dotCover/home"
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _tool.type } returns DotCoverToolType.WindowsOnly

        // act
        val result = _selector.select()

        // assert
        assertTrue(result.isFailure)
        assertThrows(Error::class.java) { result.getOrThrow() }
    }
}
