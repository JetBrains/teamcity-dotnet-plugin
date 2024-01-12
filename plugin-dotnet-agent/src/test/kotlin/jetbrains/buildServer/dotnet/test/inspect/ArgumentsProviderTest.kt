

package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType.Custom
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.inspect.ArgumentsProviderImpl
import jetbrains.buildServer.inspect.InspectCodeConstants
import jetbrains.buildServer.inspect.InspectionTool
import jetbrains.buildServer.inspect.PluginsSpecificationProvider
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ArgumentsProviderTest {
    @MockK
    private lateinit var _parametersServiceMock: ParametersService

    @MockK
    private lateinit var _pathsServiceMock: PathsService

    @MockK
    private lateinit var _fileSystemServiceMock: FileSystemService

    @MockK
    private lateinit var _pluginSpecificationsProvider: PluginsSpecificationProvider

    @BeforeMethod
    fun setUp() {
        clearAllMocks()
        MockKAnnotations.init(this)
    }

    @Test
    fun `should resolve all expected arguments`() {
        // arrange
        val configFilePath = "./config/path"
        val outputPath = "/output/path"
        val logFilePath = "./log/file/path"
        val cachesHomePath = "/caches-home/path"
        val customArgumentExtension = "CustomArgumentExtension/1.0.0"
        val dedicatedExtension = "DedicatedExtension/2.0.0"
        val toolVersion = Version.FirstInspectCodeWithExtensionsOptionVersion
        val commandLine = """
            --config=$configFilePath
            --output=$outputPath
            --logFile=$logFilePath
            --caches-home=$cachesHomePath
            --eXtensions=$customArgumentExtension
        """.trimIndent()
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { commandLine }
        val checkoutPathMock = mockk<File>().also { fileMock -> every { _pathsServiceMock.getPath(any()) } answers { fileMock } }
        val configFileMock = mockk<File>().also { every { it.path } answers { configFilePath } }
        val outputFileMock = mockk<File>().also { every { it.path } answers { outputPath } }
        val logFileMock = mockk<File>().also { every { it.path } answers { logFilePath } }
        val cacheHomeFileMock = mockk<File>().also { every { it.path } answers { cachesHomePath } }
        val absoluteConfigFileMock = mockk<File>()
        val absoluteLogFileMock = mockk<File>()
        _fileSystemServiceMock.also {
            every { it.createFile(configFilePath) } answers { configFileMock }
            every { it.createFile(outputPath) } answers { outputFileMock }
            every { it.createFile(logFilePath) } answers { logFileMock }
            every { it.createFile(cachesHomePath) } answers { cacheHomeFileMock }
            every { it.createFile(checkoutPathMock, configFilePath) } answers { absoluteConfigFileMock }
            every { it.createFile(checkoutPathMock, logFilePath) } answers { absoluteLogFileMock }
        }
        every { _fileSystemServiceMock.isAbsolute(any()) } returnsMany (listOf(false, true, false, true))
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { dedicatedExtension }
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode, toolVersion)

        // assert
        Assert.assertNotNull(result)
        verify(exactly = 0) { _fileSystemServiceMock.generateTempFile(any(), any(), any()) }

        // --config � relative
        Assert.assertEquals(result.configFile, absoluteConfigFileMock)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(configFilePath) }
        verify(exactly = 1) { _fileSystemServiceMock.createFile(checkoutPathMock, configFilePath) }

        // --output � absolute
        Assert.assertEquals(result.outputFile, outputFileMock)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(outputPath) }

        // --logFile � relative
        Assert.assertEquals(result.logFile, absoluteLogFileMock)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(logFilePath) }
        verify(exactly = 1) { _fileSystemServiceMock.createFile(checkoutPathMock, logFilePath) }

        // --caches-home � absolute
        Assert.assertEquals(result.cachesHome, cacheHomeFileMock)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(cachesHomePath) }

        // --eXtensions
        val actualExtensions = result.extensions
        Assert.assertNotNull(actualExtensions)
        actualExtensions?.let { Assert.assertTrue(it.contains(dedicatedExtension)) }
        actualExtensions?.let { Assert.assertTrue(it.contains(customArgumentExtension)) }
        actualExtensions?.let { Assert.assertEquals(it, "$dedicatedExtension;$customArgumentExtension") }
        verify(exactly = 1) { _pluginSpecificationsProvider.getPluginsSpecification() }
    }

    @DataProvider
    fun `args without specifying predefined paths`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "--Arg1",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
        arrayOf(
            "--Arg1\r\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom), CommandLineArgument("--Arg  2", Custom))
        ),
        // Custom args separated by \n (https://youtrack.jetbrains.com/issue/TW-72039)
        arrayOf(
            "--Arg1\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom), CommandLineArgument("--Arg  2", Custom))
        ),
        arrayOf(
            "--Arg1\n  \n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom), CommandLineArgument("--Arg  2", Custom))
        ),
        arrayOf(
            "--Arg1\r\n  \r\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom), CommandLineArgument("--Arg  2", Custom))
        ),
        arrayOf(
            "--Arg1\r--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", Custom), CommandLineArgument("--Arg  2", Custom))
        ),
        // Custom args with spaces (https://youtrack.jetbrains.com/issue/TW-71722)
        arrayOf(
            "--Arg  1" + System.lineSeparator() + "--Arg2",
            null,
            false,
            listOf(CommandLineArgument("--Arg  1", Custom), CommandLineArgument("--Arg2", Custom))
        ),
    )

    @Test(dataProvider = "args without specifying predefined paths")
    fun `should resolve arguments along with generating temp files`(
        commandLine: String?,
        debugSettings: String?,
        expectedDebugFlag: Boolean,
        expectedArgs: List<CommandLineArgument>
    ) {
        // arrange
        val dedicatedExtension = "DedicatedExtension/2.0.0"
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.customArgs) } answers { commandLine }
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.debugSettings) } answers { debugSettings }
        }
        val agentTmpPathMock = mockk<File>().also { fileMock -> every { _pathsServiceMock.getPath(PathType.AgentTemp) } answers { fileMock } }
        val cachePerCheckoutPathMock = mockk<File>().also { fileMock -> every { _pathsServiceMock.getPath(PathType.CachePerCheckout) } answers { fileMock } }
        val configFileMock = mockk<File>()
        val outputFileMock = mockk<File>()
        val logFileMock = mockk<File>()
        val cacheHomeFileMock = mockk<File>()
        every {
            _fileSystemServiceMock.generateTempFile(agentTmpPathMock, any(), any())
        } answers { configFileMock } andThenAnswer { outputFileMock } andThenAnswer { logFileMock } andThenAnswer { cacheHomeFileMock }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { dedicatedExtension }
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.configFile, configFileMock)
        Assert.assertEquals(result.outputFile, outputFileMock)
        Assert.assertEquals(result.logFile, logFileMock)
        Assert.assertEquals(result.cachesHome, cachePerCheckoutPathMock)
        Assert.assertEquals(result.debug, expectedDebugFlag)
        val actualExtensions = result.extensions
        Assert.assertNotNull(actualExtensions)
        actualExtensions?.let { Assert.assertEquals(it, dedicatedExtension) }
        Assert.assertEquals(result.customArguments, expectedArgs)
        verify(exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, ".config") }
        verify(exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, "inspectcode-report", ".xml") }
        verify(exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, ".log") }
        verify(exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, "") }
        verify(exactly = 1) { _pluginSpecificationsProvider.getPluginsSpecification() }
    }

    @DataProvider
    fun `args with specifying predefined paths`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--config=Cfg.xml",
            "Cfg.xml",
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--output=Out.xml",
            "Out.xml",
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "-o=Out.xml",
            "Out.xml",
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--logFile=Abc.log",
            "Abc.log",
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--caches-home=Cache",
            "Cache",
            listOf(CommandLineArgument("--Arg1", Custom))
        ),
    )

    @Test(dataProvider = "args with specifying predefined paths")
    fun `should resolve arguments with specifying predefined files`(commandLine: String?, path: String, expectedArgs: List<CommandLineArgument>) {
        // arrange
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.customArgs) } answers { commandLine }
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.debugSettings) } answers { "" }
        }
        every { _pathsServiceMock.getPath(any()) } answers { mockk() }
        val fileMock = mockk<File>()
        _fileSystemServiceMock.also {
            every { it.generateTempFile(any(), any(), any()) } answers { mockk() }
            every { it.createFile(any()) } answers { fileMock }
            every { it.isAbsolute(any()) } answers { true }
        }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { null }
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.customArguments, expectedArgs)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(path) }
    }

    @Test
    fun `should resolve extensions only from custom params`() {
        // arrange
        val customExtensions = "CustomArgumentExtension/1.0.0"
        val customArgs = "--Arg1" + System.lineSeparator() + "--eXtensions=$customExtensions"
        val expectedCustomArgs = listOf(CommandLineArgument("--Arg1", Custom))
        _parametersServiceMock.also {
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.customArgs) } answers { customArgs }
            every { it.tryGetParameter(ParameterType.Runner, InspectionTool.Inspectcode.debugSettings) } answers { "" }
        }
        every { _pathsServiceMock.getPath(any()) } answers { mockk() }
        val fileMock = mockk<File>()
        _fileSystemServiceMock.also {
            every { it.generateTempFile(any(), any(), any()) } answers { mockk() }
            every { it.createFile(any()) } answers { fileMock }
            every { it.isAbsolute(any()) } answers { true }
        }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { null }
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.customArguments, expectedCustomArgs)
        val actualExtensions = result.extensions
        Assert.assertNotNull(actualExtensions)
        actualExtensions?.let { Assert.assertEquals(actualExtensions, customExtensions) }
        verify(exactly = 1) { _pluginSpecificationsProvider.getPluginsSpecification() }
    }

    @Test
    fun `should resolve valid absolute cache home path when it provided with quotes`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "--caches-home=\"/absolute/path\"" }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } answers { mockk() } } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { true }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { null }
        val provider = createInstance()

        // act
        provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        verify(exactly = 1) { _fileSystemServiceMock.createFile("/absolute/path") }
    }

    @Test
    fun `should resolve valid relative cache home path when it provided with quotes`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "--caches-home='./relative/path'" }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } returns ("") } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { false }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        every { _fileSystemServiceMock.createFile(any(), any()) } answers { mockk() }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { null }
        val provider = createInstance()

        // act
        provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        verify(exactly = 1) { _fileSystemServiceMock.createFile("./relative/path") }
    }

    @DataProvider
    fun `extensions resolved test data`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "--eXtensions=Custom.Extension1/1.0.0;Custom.Extension2/2.0.0",
            "Dedicated.Extension1/1.0.0;Dedicated.Extension2/2.0.0",
            "Dedicated.Extension1/1.0.0;Dedicated.Extension2/2.0.0;Custom.Extension1/1.0.0;Custom.Extension2/2.0.0"
        ),
        arrayOf(
            null,
            "Dedicated.Extension1/1.0.0;Dedicated.Extension2/2.0.0",
            "Dedicated.Extension1/1.0.0;Dedicated.Extension2/2.0.0"
        ),
        arrayOf(
            "-x=Custom.Extension1/1.0.0;Custom.Extension2/2.0.0",
            null,
            "Custom.Extension1/1.0.0;Custom.Extension2/2.0.0"
        ),
        arrayOf(
            null,
            null,
            null
        )
    )

    @Test(dataProvider = "extensions resolved test data")
    fun `should properly set extensions and cut from custom arguments when tool is inspectcode and version is high enough`(
        extensionsCustomArgument: String?,
        extensionSpecification: String?,
        expectedExtensions: String?
    ) {
        // arrange
        val firstArg = "--Arg1=\"abc\""
        val lastArg = "--Arg2 1234"
        val customArgs = buildString {
            append(firstArg)
            append(System.lineSeparator())
            extensionsCustomArgument?.let {
                append(it)
                append(System.lineSeparator())
            }
            append(lastArg)
        }
        val expectedCustomArgs = listOf(
            CommandLineArgument(firstArg, Custom),
            CommandLineArgument(lastArg, Custom)
        )
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { customArgs }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } returns ("") } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { false }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        every { _fileSystemServiceMock.createFile(any(), any()) } answers { mockk() }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { extensionSpecification }
        val provider = createInstance()

        // act
        val arguments = provider.getArguments(InspectionTool.Inspectcode, Version.FirstInspectCodeWithExtensionsOptionVersion)

        // assert
        Assert.assertEquals(arguments.extensions, expectedExtensions)
        Assert.assertEquals(arguments.customArguments, expectedCustomArgs)
        verify(exactly = 1) { _pluginSpecificationsProvider.getPluginsSpecification() }
    }

    @DataProvider
    fun `extensions not resolved test data`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            Version.Empty,
            InspectionTool.Inspectcode,
        ),
        arrayOf(
            Version(2021, 2, 999),
            InspectionTool.Inspectcode
        ),
        arrayOf(
            Version.FirstInspectCodeWithExtensionsOptionVersion,
            InspectionTool.Dupfinder
        ),
        arrayOf(
            Version(2023, 0, 0),
            InspectionTool.Dupfinder
        ),
    )

    @Test(dataProvider = "extensions not resolved test data")
    fun `should not set extensions and cut from custom arguments when tool is not inspectcode and version is too low`(
        toolVersion: Version,
        toolType: InspectionTool
    ) {
        // arrange
        val extensionSpecification = "Dedicated.Extension1/1.0.0;Dedicated.Extension2/2.0.0"
        val arg1 = "--Arg1=\"abc\""
        val arg2 = "--eXtensions=Custom.Extension1/1.0.0;Custom.Extension2/2.0.0"
        val arg3 = "--Arg2 1234"
        val customArgs = "$arg1${System.lineSeparator()}$arg2${System.lineSeparator()}$arg3"
        val expectedCustomArgs = listOf(CommandLineArgument(arg1, Custom), CommandLineArgument(arg2, Custom), CommandLineArgument(arg3, Custom))
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { customArgs }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } returns ("") } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { false }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        every { _fileSystemServiceMock.createFile(any(), any()) } answers { mockk() }
        every { _pluginSpecificationsProvider.getPluginsSpecification() } answers { extensionSpecification }
        val provider = createInstance()

        // act
        val arguments = provider.getArguments(toolType, toolVersion)

        // assert
        Assert.assertNull(arguments.extensions)
        Assert.assertEquals(arguments.customArguments, expectedCustomArgs)
        verify(exactly = 0) { _pluginSpecificationsProvider.getPluginsSpecification() }
    }

    private fun createInstance() = ArgumentsProviderImpl(_parametersServiceMock, _pathsServiceMock, _fileSystemServiceMock, _pluginSpecificationsProvider)
}