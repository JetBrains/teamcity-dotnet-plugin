package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.LoggerResolver
import jetbrains.buildServer.dotnet.ToolType
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironment
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironmentImpl
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.BeforeMethod
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

    @Test
    fun shouldCopyLoggerDirToParentDirOfTargetWhenHasTargets() {
        // Given
        val workingDirectory = File("wd")
        val loggerFile = File("vstest15", "logger.dll")
        val targetFile1 = File("proj", "my1.csproj")
        val targetFile2 = File("my2.sln")
        val uniqueName1 = "123"
        val uniqueName2 = "abc"
        val uniqueNameWd = "xyz"
        val loggerEnvironment = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName1))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName2))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueNameWd))

                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(targetFile1.absoluteFile.parentFile, uniqueName1))
                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(targetFile2.absoluteFile.parentFile, uniqueName2))
                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(workingDirectory.absoluteFile, uniqueNameWd))
            }
        })

        loggerEnvironment.configure(listOf(targetFile1, targetFile2))

        // Then
        _ctx!!.assertIsSatisfied()
    }

    @Test
    fun shouldRemoveLoggerDirFromParentDirOfTargetWhenFinish() {
        // Given
        val workingDirectory = File("wd")
        val loggerFile = File("vstest15", "logger.dll")
        val targetFile1 = File("proj", "my1.csproj")
        val targetFile2 = File("my2.sln")
        val uniqueName1 = "123"
        val uniqueName2 = "abc"
        val uniqueNameWd = "xyz"
        val loggerEnvironment = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                allowing<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName1))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName2))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueNameWd))

                allowing<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(targetFile1.absoluteFile.parentFile, uniqueName1))
                allowing<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(targetFile2.absoluteFile.parentFile, uniqueName2))
                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(workingDirectory.absoluteFile, uniqueNameWd))

                oneOf<FileSystemService>(_fileSystemService).remove(File(targetFile1.absoluteFile.parentFile, uniqueName1))
                oneOf<FileSystemService>(_fileSystemService).remove(File(targetFile2.absoluteFile.parentFile, uniqueName2))
                oneOf<FileSystemService>(_fileSystemService).remove(File(workingDirectory.absoluteFile, uniqueNameWd))
            }
        })

        loggerEnvironment.configure(listOf(targetFile1, targetFile2)).close()

        // Then
        _ctx!!.assertIsSatisfied()
    }

    @Test
    fun shouldCopyLoggerDirToParentDirOfTargetWhenHasSameBaseDirectoryForTargets() {
        // Given
        val workingDirectory = File("wd")
        val loggerFile = File("vstest15", "logger.dll")
        val targetFile1 = File("proj", "my1.csproj")
        val targetFile2 = File("proj", "my2.sln")
        val uniqueName = "abc"
        val uniqueNameWd = "xyz"
        val loggerEnvironment = createInstance()

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueNameWd))

                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(targetFile1.absoluteFile.parentFile, uniqueName))
                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(workingDirectory.absoluteFile, uniqueNameWd))
            }
        })

        loggerEnvironment.configure(listOf(targetFile1, targetFile2))

        // Then
        _ctx!!.assertIsSatisfied()
    }

    @Test
    fun shouldCopyLoggerDirToWorkingDirOfTargetWhenHasNoTargets() {
        // Given
        val workingDirectory = File("wd")
        val loggerFile = File("vstest15", "logger.dll")
        val loggerEnvironment = createInstance()
        val uniqueName = "123"

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))

                oneOf<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(workingDirectory.absoluteFile, uniqueName))
            }
        })

        loggerEnvironment.configure(emptyList())

        // Then
        _ctx!!.assertIsSatisfied()
    }

    @Test
    fun shouldRemoveLoggerDirFromWorkingDirOfTargetWhenHasNoTargets() {
        // Given
        val workingDirectory = File("wd")
        val loggerFile = File("vstest15", "logger.dll")
        val loggerEnvironment = createInstance()
        val uniqueName = "123"

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<LoggerResolver>(_loggerResolver).resolve(ToolType.VSTest)
                will(returnValue(loggerFile))

                oneOf<PathsService>(_pathService).getPath(PathType.WorkingDirectory)
                will(returnValue(workingDirectory))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(uniqueName))

                allowing<FileSystemService>(_fileSystemService).copy(loggerFile.parentFile, File(workingDirectory.absoluteFile, uniqueName))
                oneOf<FileSystemService>(_fileSystemService).remove(File(workingDirectory.absoluteFile, uniqueName))
            }
        })

        loggerEnvironment.configure(emptyList()).close()

        // Then
        _ctx!!.assertIsSatisfied()
    }

    private fun createInstance(): VSTestLoggerEnvironment {
        return VSTestLoggerEnvironmentImpl(
                _pathService!!,
                _fileSystemService!!,
                _loggerResolver!!)
    }
}