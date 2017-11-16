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
import org.testng.annotations.Test
import java.io.File

class VSTestLoggerEnvironmentCleanerTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _fileSystemService: FileSystemService? = null
    private var _loggerService: LoggerService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _fileSystemService = _ctx!!.mock(FileSystemService::class.java)
        _loggerService = _ctx!!.mock(LoggerService::class.java)
    }

    @Test
    fun shouldClean() {
        // Given
        val checkoutDir = File("checkoutDir")
        val loggerDir1 = File(checkoutDir, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}loggerdir")
        val loggerDir2 = File(checkoutDir, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}2313123")
        val dir1 = File(checkoutDir, "2313123${VSTestLoggerEnvironmentImpl.DirectoryPrefix}")
        val dir2 = File(checkoutDir, "2313123${VSTestLoggerEnvironmentImpl.DirectoryPrefix}")
        val dir3 = File("abc", "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}loggerdir")
        val file1 = File(checkoutDir, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}abc")

        val fileSystemService = VirtualFileSystemService()
                .addDirectory(checkoutDir)
                .addDirectory(loggerDir1)
                .addDirectory(dir1)
                .addDirectory(loggerDir2)
                .addDirectory(dir2)
                .addDirectory(dir3)
                .addFile(file1)

        val environmentCleaner = VSTestLoggerEnvironmentCleanerImpl(
                _pathService!!,
                fileSystemService,
                _loggerService!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDir))
            }
        })

        environmentCleaner.clean()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(fileSystemService.isExists(checkoutDir), true)
        Assert.assertEquals(fileSystemService.isExists(loggerDir1), false)
        Assert.assertEquals(fileSystemService.isExists(loggerDir2), false)
        Assert.assertEquals(fileSystemService.isExists(dir1), true)
        Assert.assertEquals(fileSystemService.isExists(dir2), true)
        Assert.assertEquals(fileSystemService.isExists(dir3), true)
        Assert.assertEquals(fileSystemService.isExists(file1), true)
    }

    @Test
    fun shouldLogError() {
        // Given
        val checkoutDir = File("checkoutDir")
        val loggerDir1 = File(checkoutDir, "${VSTestLoggerEnvironmentImpl.DirectoryPrefix}loggerdir")
        val error = Exception("some error")

        val fileSystemService = VirtualFileSystemService()
                .addDirectory(checkoutDir)
                .addDirectory(loggerDir1, VirtualFileSystemService.errorOnRemove(error))

        val environmentCleaner = VSTestLoggerEnvironmentCleanerImpl(
                _pathService!!,
                fileSystemService,
                _loggerService!!)

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.Checkout)
                will(returnValue(checkoutDir))

                oneOf<LoggerService>(_loggerService).onErrorOutput("Failed to remove logger directory \"$loggerDir1\"")
            }
        })

        environmentCleaner.clean()

        // Then
        _ctx!!.assertIsSatisfied()
    }
}