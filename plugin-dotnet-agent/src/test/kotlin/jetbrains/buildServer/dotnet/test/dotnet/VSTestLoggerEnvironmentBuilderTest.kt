package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerEnvironmentBuilderTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _loggerResolver: LoggerResolver? = null
    private var _fileSystemService: FileSystemService? = null
    private var _loggerService: LoggerService? = null
    private var _environmentCleaner: EnvironmentCleaner? = null
    private var _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer? = null
    private var _testReportingParameters: TestReportingParameters? = null
    private var _dotnetCommand: DotnetCommand?= null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _fileSystemService = _ctx!!.mock(FileSystemService::class.java)
        _loggerResolver = _ctx!!.mock(LoggerResolver::class.java)
        _loggerService = _ctx!!.mock(LoggerService::class.java)
        _environmentCleaner = _ctx!!.mock(EnvironmentCleaner::class.java)
        _environmentAnalyzer = _ctx!!.mock(VSTestLoggerEnvironmentAnalyzer::class.java)
        _testReportingParameters = _ctx!!.mock(TestReportingParameters::class.java)
        _dotnetCommand = _ctx!!.mock(DotnetCommand::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // one project in checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        sequenceOf(TargetArguments(sequenceOf(CommandLineArgument(File("dir", "my.proj").path)))),
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true) )
                                .addFile(File(File(File("checkoutDir").absoluteFile, "dir"), "my.proj")),
                        listOf(
                                File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentBuilder.DirectoryPrefix}abc"),
                                File(File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentBuilder.DirectoryPrefix}abc"), VSTestLoggerEnvironmentBuilder.ReadmeFileName))))
    }

    @Test(dataProvider = "testData")
    fun shouldCopyLoggerAndCreateReadme(
            checkoutDirectory: File,
            targetArguments: Sequence<TargetArguments>,
            targetFiles: List<File>,
            fileSystemService: VirtualFileSystemService,
            expectedDirs: List<File>) {
        // Given
        val loggerFile = File("vstest15", "logger.dll")
        fileSystemService.addFile(loggerFile.absoluteFile)

        val uniqueName = "abc"
        val loggerEnvironment = VSTestLoggerEnvironmentBuilder(
                _pathService!!,
                fileSystemService,
                _loggerResolver!!,
                _loggerService!!,
                _testReportingParameters!!,
                _environmentCleaner!!,
                _environmentAnalyzer!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(_testReportingParameters).mode
                will(returnValue(TestReportingMode.On))

                oneOf<DotnetCommand>(_dotnetCommand).targetArguments
                will(returnValue(targetArguments))

                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDirectory))

                oneOf<EnvironmentCleaner>(_environmentCleaner).clean()

                oneOf<VSTestLoggerEnvironmentAnalyzer>(_environmentAnalyzer).analyze(targetFiles)

                allowing<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))
            }
        })

        var ticket = loggerEnvironment.build(_dotnetCommand!!)

        // Then
        _ctx!!.assertIsSatisfied()
        for (expectedDir in expectedDirs) {
            Assert.assertEquals(fileSystemService.isExists(expectedDir), true)
        }

        ticket.close()
        for (expectedDir in expectedDirs) {
            val dir = File(expectedDir, uniqueName)
            Assert.assertEquals(fileSystemService.isExists(dir), false)
        }
    }

    @Test
    fun shouldNotInjectLoggerWhenTestReportingIsOff() {
        // Given
        val targetFiles = listOf(File("dir", "my.proj"))
        val loggerEnvironment = VSTestLoggerEnvironmentBuilder(
                _pathService!!,
                _fileSystemService!!,
                _loggerResolver!!,
                _loggerService!!,
                _testReportingParameters!!,
                _environmentCleaner!!,
                _environmentAnalyzer!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<TestReportingParameters>(_testReportingParameters).mode
                will(returnValue(TestReportingMode.Off))

                never<DotnetCommand>(_dotnetCommand).targetArguments

                never<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)

                never<PathsService>(_pathService).getPath(PathType.Checkout)

                never<EnvironmentCleaner>(_environmentCleaner).clean()

                never<VSTestLoggerEnvironmentAnalyzer>(_environmentAnalyzer).analyze(targetFiles)

                never<PathsService>(_pathService).uniqueName
            }
        })

        loggerEnvironment.build(_dotnetCommand!!).close()

        // Then
        _ctx!!.assertIsSatisfied()
    }
}