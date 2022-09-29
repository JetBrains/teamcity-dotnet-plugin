package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.inspect.ArgumentsProviderImpl
import jetbrains.buildServer.inspect.InspectCodeConstants
import jetbrains.buildServer.inspect.InspectionTool
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
        val commandLine = """
            --config=$configFilePath
            --output=$outputPath
            --logFile=$logFilePath
            --caches-home=$cachesHomePath
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
        every { _fileSystemServiceMock.isAbsolute(any()) } returnsMany(listOf(false, true, false, true))
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode)

        // assert
        Assert.assertNotNull(result)
        verify (exactly = 0) { _fileSystemServiceMock.generateTempFile(any(), any(), any()) }

        // --config � relative
        Assert.assertEquals(result.configFile, absoluteConfigFileMock)
        verify (exactly = 1) { _fileSystemServiceMock.createFile(configFilePath) }
        verify (exactly = 1) { _fileSystemServiceMock.createFile(checkoutPathMock, configFilePath) }

        // --output � absolute
        Assert.assertEquals(result.outputFile, outputFileMock)
        verify (exactly = 1) { _fileSystemServiceMock.createFile(outputPath) }

        // --logFile � relative
        Assert.assertEquals(result.logFile, absoluteLogFileMock)
        verify (exactly = 1) { _fileSystemServiceMock.createFile(logFilePath) }
        verify (exactly = 1) { _fileSystemServiceMock.createFile(checkoutPathMock, logFilePath) }

        // --caches-home � absolute
        Assert.assertEquals(result.cachesHome, cacheHomeFileMock)
        verify (exactly = 1) { _fileSystemServiceMock.createFile(cachesHomePath) }
    }

    @DataProvider
    fun `args without specifying predefined paths`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "--Arg1",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1\r\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
        ),
        // Custom args separated by \n (https://youtrack.jetbrains.com/issue/TW-72039)
        arrayOf(
            "--Arg1\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1\n  \n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1\r\n  \r\n--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1\r--Arg  2",
            null,
            false,
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
        ),
        // Custom args with spaces (https://youtrack.jetbrains.com/issue/TW-71722)
        arrayOf(
            "--Arg  1" + System.lineSeparator() + "--Arg2",
            null,
            false,
            listOf(CommandLineArgument("--Arg  1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg2", CommandLineArgumentType.Custom))
        ),
    )

    @Test(dataProvider = "args without specifying predefined paths")
    fun `should resolve arguments along with generating temp files`(commandLine: String?, debugSettings: String?, expectedDebugFlag: Boolean, expectedArgs: List<CommandLineArgument>) {
        // arrange
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
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.configFile, configFileMock)
        Assert.assertEquals(result.outputFile, outputFileMock)
        Assert.assertEquals(result.logFile, logFileMock)
        Assert.assertEquals(result.cachesHome, cachePerCheckoutPathMock)
        Assert.assertEquals(result.debug, expectedDebugFlag)
        Assert.assertEquals(result.customArguments, expectedArgs)
        verify (exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, ".config") }
        verify (exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, "inspectcode-report", ".xml") }
        verify (exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, ".log") }
        verify (exactly = 1) { _fileSystemServiceMock.generateTempFile(agentTmpPathMock, InspectCodeConstants.RUNNER_TYPE, "") }
    }

    @DataProvider
    fun `args with specifying predefined paths`(): Array<Array<out Any?>> = arrayOf(
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--config=Cfg.xml",
            "Cfg.xml",
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--output=Out.xml",
            "Out.xml",
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "-o=Out.xml",
            "Out.xml",
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--logFile=Abc.log",
            "Abc.log",
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
        ),
        arrayOf(
            "--Arg1" + System.lineSeparator() + "--caches-home=Cache",
            "Cache",
            listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
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
        val provider = createInstance()

        // act
        val result = provider.getArguments(InspectionTool.Inspectcode)

        // assert
        Assert.assertNotNull(result)
        Assert.assertEquals(result.customArguments, expectedArgs)
        verify(exactly = 1) { _fileSystemServiceMock.createFile(path) }
    }

    @Test
    fun `should resolve valid absolute cache home path when it provided with quotes`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "--caches-home=\"/absolute/path\"" }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } answers { mockk() } } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { true }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        val provider = createInstance()

        // act
        provider.getArguments(InspectionTool.Inspectcode)

        // assert
        verify (exactly = 1) { _fileSystemServiceMock.createFile("/absolute/path") }
    }

    @Test
    fun `should resolve valid relative cache home path when it provided with quotes`() {
        // arrange
        every { _parametersServiceMock.tryGetParameter(any(), any()) } answers { "--caches-home='./relative/path'" }
        every { _fileSystemServiceMock.createFile(any()) } answers { mockk<File>().also { every { it.path } returns("") } }
        every { _fileSystemServiceMock.isAbsolute(any()) } answers { false }
        every { _fileSystemServiceMock.generateTempFile(any(), any(), any()) } answers { mockk() }
        every { _pathsServiceMock.getPath(any()) } answers { mockk<File>() }
        every { _fileSystemServiceMock.createFile(any(), any()) } answers { mockk() }
        val provider = createInstance()

        // act
        provider.getArguments(InspectionTool.Inspectcode)

        // assert
        verify (exactly = 1) { _fileSystemServiceMock.createFile("./relative/path") }
    }

    private fun createInstance() = ArgumentsProviderImpl(_parametersServiceMock,_pathsServiceMock,_fileSystemServiceMock)
}