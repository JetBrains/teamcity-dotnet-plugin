package jetbrains.buildServer.dotnet.test.dotnet

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

class VSTestLoggerEnvironmentTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _loggerResolver: LoggerResolver? = null
    private var _fileSystemService: FileSystemService? = null
    private var _loggerService: LoggerService? = null
    private var _environmentCleaner: VSTestLoggerEnvironmentCleaner? = null
    private var _environmentAnalyzer: VSTestLoggerEnvironmentAnalyzer? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _fileSystemService = _ctx!!.mock(FileSystemService::class.java)
        _loggerResolver = _ctx!!.mock(LoggerResolver::class.java)
        _loggerService = _ctx!!.mock(LoggerService::class.java)
        _environmentCleaner = _ctx!!.mock(VSTestLoggerEnvironmentCleaner::class.java)
        _environmentAnalyzer = _ctx!!.mock(VSTestLoggerEnvironmentAnalyzer::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // one project in checkout dir
                arrayOf(
                        File("checkoutDir").absoluteFile,
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("checkoutDir").absoluteFile, VirtualFileSystemService.absolute(true) )
                                .addFile(File(File(File("checkoutDir").absoluteFile, "dir"), "my.proj")),
                        listOf(
                                File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}abc"),
                                File(File(File("checkoutDir").absoluteFile, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}abc"), VSTestLoggerEnvironmentImpl.ReadmeFileName))))
    }

    @Test(dataProvider = "testData")
    fun shouldCopyLoggerAndCreateReadme(
            checkoutDirectory: File,
            targetFiles: List<File>,
            fileSystemService: VirtualFileSystemService,
            expectedDirs: List<File>) {
        // Given
        val loggerFile = File("vstest15", "logger.dll")
        fileSystemService.addFile(loggerFile.absoluteFile)

        val uniqueName = "abc"
        val loggerEnvironment = VSTestLoggerEnvironmentImpl(
                _pathService!!,
                fileSystemService,
                _loggerResolver!!,
                _loggerService!!,
                _environmentCleaner!!,
                _environmentAnalyzer!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDirectory))

                oneOf<VSTestLoggerEnvironmentCleaner>(_environmentCleaner).clean()

                oneOf<VSTestLoggerEnvironmentAnalyzer>(_environmentAnalyzer).analyze(targetFiles)

                allowing<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))
            }
        })

        var ticket = loggerEnvironment.configure(targetFiles)

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
}