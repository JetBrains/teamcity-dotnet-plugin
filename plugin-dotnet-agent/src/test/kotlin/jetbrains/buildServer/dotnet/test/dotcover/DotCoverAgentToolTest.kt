package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_HOME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotCoverAgentToolTest {
    @MockK private val _parametersService = mockk<ParametersService>(relaxed = true)
    @MockK private val _fileSystemService = mockk<FileSystemService>(relaxed = true)
    private lateinit var _tool: DotCoverAgentTool

    @BeforeMethod
    fun setUp() {
        _tool = DotCoverAgentTool(_parametersService, _fileSystemService)
    }

    @Test
    fun `should return correct dotCover home path when value is present in parameters`() {
        every { _parametersService.tryGetParameter(ParameterType.Runner, PARAM_DOTCOVER_HOME) } returns "somePath"

        // act
        val result = _tool.dotCoverHomePath

        // assert
        Assert.assertEquals(result, "somePath")
    }

    @Test
    fun `should return empty dotCover home path when value is null or blank`() {
        // assert
        every { _parametersService.tryGetParameter(ParameterType.Runner, PARAM_DOTCOVER_HOME) } returns null

        // act
        val result = _tool.dotCoverHomePath

        // assert
        Assert.assertEquals(result, "")
    }

    @Test
    fun `should return Windows-only tool type when exe exists and dll does not exist`() {
        // assert
        every { _fileSystemService.isExists(any()) } answers { arg<File>(0).name == "dotCover.exe" }

        // act
        val result = _tool.type

        // assert
        Assert.assertEquals(result, DotCoverToolType.WindowsOnly)
    }

    @Test
    fun `should return cross-platform tool type when dll exists and sh does not exist`() {
        // assert
        every { _fileSystemService.isExists(any()) } answers { arg<File>(0).name == "dotCover.dll" }

        // act
        val result = _tool.type

        // assert
        Assert.assertEquals(result, DotCoverToolType.CrossPlatform)
    }

    @Test
    fun `should return deprecated cross-platform tool type when sh exists`() {
        // assert
        every { _fileSystemService.isExists(any()) }answers
            { sequenceOf("dotCover.sh", "dotCover.exe", "dotCover.dll").contains(arg<File>(0).name) }

        // act
        val result = _tool.type

        // assert
        Assert.assertEquals(result, DotCoverToolType.DeprecatedCrossPlatform)
    }

    @DataProvider(name = "dotnet runtime detected params")
    fun `dotnet runtime detected params`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(sequenceOf("${CONFIG_PREFIX_CORE_RUNTIME}3.1${CONFIG_SUFFIX_PATH}"), true),
            arrayOf(sequenceOf("${CONFIG_PREFIX_CORE_RUNTIME}7.0.403$CONFIG_SUFFIX_PATH"), true),
            arrayOf(sequenceOf("${CONFIG_PREFIX_CORE_RUNTIME}8-preview1$CONFIG_SUFFIX_PATH"), true),
            arrayOf(sequenceOf("${CONFIG_PREFIX_CORE_RUNTIME}3.0$CONFIG_SUFFIX_PATH"), false),
            arrayOf(emptySequence<String>(), false),
        )
    }

    @Test(dataProvider = "dotnet runtime detected params")
    fun `should can use DotNet runtime when dll exists and requirement is satisfied by build configuration parameters`(
        params: Sequence<String>,
        expectedCompatibility: Boolean
    ) {
        // assert
        every { _fileSystemService.isExists(any()) } answers { arg<File>(0).name == "dotCover.dll" }
        every { _parametersService.getParameterNames(ParameterType.Configuration) } returns params

        // act
        val result = _tool.canUseDotNetRuntime

        // assert
        Assert.assertEquals(result, expectedCompatibility)
    }

    @DataProvider(name = "dotnet framework runtime detected params")
    fun `dotnet framework runtime detected params`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(sequenceOf("${CONFIG_PREFIX_DOTNET_FRAMEWORK}4.7.2${CONFIG_SUFFIX_PATH}"), true),
            arrayOf(sequenceOf("${CONFIG_PREFIX_DOTNET_FRAMEWORK}4.8${CONFIG_SUFFIX_PATH}"), true),
            arrayOf(sequenceOf("${CONFIG_PREFIX_DOTNET_FRAMEWORK}4.6.1${CONFIG_SUFFIX_PATH}"), false),
            arrayOf(sequenceOf("${CONFIG_PREFIX_DOTNET_FRAMEWORK}4.5${CONFIG_SUFFIX_PATH}"), false),
            arrayOf(sequenceOf("${CONFIG_PREFIX_DOTNET_FRAMEWORK}4.0${CONFIG_SUFFIX_PATH}"), false),
            arrayOf(emptySequence<String>(), false),
        )
    }

    @Test(dataProvider = "dotnet framework runtime detected params")
    fun `should can use DotNet Framework runtime when exe exists and requirement is satisfied by build configuration parameters`(
        params: Sequence<String>,
        expectedCompatibility: Boolean
    ) {
        // assert
        every { _fileSystemService.isExists(any()) } answers { arg<File>(0).name == "dotCover.exe" }
        every { _parametersService.getParameterNames(ParameterType.Configuration) } returns params

        // act
        val result = _tool.canUseDotNetFrameworkRuntime

        // assert
        Assert.assertEquals(result, expectedCompatibility)
    }

    @Test
    fun `should get correct cross-platform version min requirement for windows`() {
        // act
        val result = _tool.getCrossPlatformVersionMinRequirement(OSType.WINDOWS).toList()

        // assert
        Assert.assertEquals(result.count(), 2)
        Assert.assertTrue(result.contains(".NET Core 3.1+") && result.contains(".NET Framework 4.7.2+"))
    }

    @Test
    fun `should get correct cross-platform version min requirement for non-windows`() {
        // act
        val result = _tool.getCrossPlatformVersionMinRequirement(OSType.UNIX).toList()

        // assert
        Assert.assertEquals(result.count(), 1)
        Assert.assertTrue(result.contains(".NET Core 3.1+"))
    }
}
