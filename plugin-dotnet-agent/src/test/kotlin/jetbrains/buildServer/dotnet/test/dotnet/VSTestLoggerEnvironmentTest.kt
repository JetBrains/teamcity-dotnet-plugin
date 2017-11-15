package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParametersService
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
    private var _parametersService: ParametersService? = null
    private var _loggerResolver: LoggerResolver? = null
    private var _fileSystemService: FileSystemService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _fileSystemService = _ctx!!.mock(FileSystemService::class.java)
        _parametersService = _ctx!!.mock(ParametersService::class.java)
        _loggerResolver = _ctx!!.mock(LoggerResolver::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // one project in checkout dir
                arrayOf(
                        File("wd").absoluteFile,
                        listOf(File("dir", "my.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("wd").absoluteFile, VirtualFileSystemService.Attributes(true))
                                .addFile(File("dir", "my.proj"), VirtualFileSystemService.Attributes(false)),
                        listOf(
                                File("wd").absoluteFile,
                                File(File("wd"), "dir").absoluteFile)),

                // one project's dir in checkout dir
                arrayOf(
                        File("wd").absoluteFile,
                        listOf(File("dir")),
                        VirtualFileSystemService()
                                .addDirectory(File("wd").absoluteFile, VirtualFileSystemService.Attributes(true))
                                .addDirectory(File(File("wd"),"dir").absoluteFile, VirtualFileSystemService.Attributes(false)),
                        listOf(
                                File("wd").absoluteFile,
                                File(File("wd"), "dir").absoluteFile)),

                // several projects in checkout dir
                arrayOf(
                        File("wd").absoluteFile,
                        listOf(File("dir", "my.proj"), File("dir2", "my2.proj")),
                        VirtualFileSystemService()
                                .addDirectory(File("wd").absoluteFile, VirtualFileSystemService.Attributes(true))
                                .addFile(File("dir", "my.proj"), VirtualFileSystemService.Attributes(false)),
                        listOf(
                                File("wd").absoluteFile,
                                File(File("wd"), "dir").absoluteFile,
                                File(File("wd"), "dir2").absoluteFile)),

                // project dir the same as checkout dir
                arrayOf(
                        File("dir").absoluteFile,
                        listOf(File(File("dir").absoluteFile, "my.proj")),
                        VirtualFileSystemService()
                                .addFile(File("dir", "my.proj"), VirtualFileSystemService.Attributes(false)),
                        listOf(
                                File("dir").absoluteFile)),

                // one project in not checkout dir
                arrayOf(
                        File("wd").absoluteFile,
                        listOf(File("dir", "my.proj").absoluteFile),
                        VirtualFileSystemService()
                                .addDirectory(File("wd").absoluteFile, VirtualFileSystemService.Attributes(true))
                                .addFile(File("dir", "my.proj").absoluteFile, VirtualFileSystemService.Attributes(true)),
                        listOf(
                                File("wd").absoluteFile,
                                File("dir").absoluteFile)),

                // project dir the same as working dir
                arrayOf(
                        File("dir").absoluteFile,
                        listOf(File(File("dir").absoluteFile, "my.proj").absoluteFile),
                        VirtualFileSystemService()
                                .addFile(File("dir", "my.proj").absoluteFile, VirtualFileSystemService.Attributes(true)),
                        listOf(
                                File("dir").absoluteFile)))
    }

    @Test(dataProvider = "testData")
    fun shouldCopyLogger(
            workingDirectory: File,
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
                _loggerResolver!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                allowing<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))
            }
        })

        var ticket = loggerEnvironment.configure(targetFiles)

        // Then
        _ctx!!.assertIsSatisfied()
        for (expectedDir in expectedDirs) {
            val dir = File(expectedDir, uniqueName)
            Assert.assertEquals(fileSystemService.isExists(dir), true)
            Assert.assertEquals(fileSystemService.isDirectory(dir), true)
        }

        ticket.close()
        for (expectedDir in expectedDirs) {
            val dir = File(expectedDir, uniqueName)
            Assert.assertEquals(fileSystemService.isExists(dir), false)
        }
    }

    private fun createInstance(): VSTestLoggerEnvironment {
        return VSTestLoggerEnvironmentImpl(
                _pathService!!,
                _fileSystemService!!,
                _loggerResolver!!)
    }
}