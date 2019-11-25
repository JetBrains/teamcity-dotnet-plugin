package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariablesImpl
import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class EnvironmentVariablesTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
    }

    @Test
    fun shouldProvideDefaultVars() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath))).toList())
    }

    @Test
    fun shouldUseSharedCompilation() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath))).toList())
    }

    @DataProvider(name = "osTypesData")
    fun osTypesData(): Array<Array<OSType>> {
        return arrayOf(
                arrayOf(OSType.UNIX),
                arrayOf(OSType.MAC))
    }

    @Test(dataProvider = "osTypesData")
    fun shouldProvideDefaultVarsWhenVirtualContextFromWindows(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath)) + EnvironmentVariablesImpl.getTempDirVariables()).toList())
    }

    @Test
    fun shouldProvideDefaultVarsWhenVirtualContextForWindowsContainer() {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath))).toList())
    }

    @Test(dataProvider = "osTypesData")
    fun shouldNotOverrideTeamCityTempWhenNotVirtualAndNotWindowsAndLenghtLessOrEq60(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(60)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(tempPath)) {
            Assert.assertTrue(!actualVariables.contains(envVar))
        }
    }

    @Test(dataProvider = "osTypesData")
    fun shouldOverrideTeamCityTempByTmpWhenNotVirtualAndNotWindowsAndLenghtLessMore60AndTmpExists(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(61)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.defaultTemp) } returns true
        every { _fileSystemService.isDirectory(EnvironmentVariablesImpl.defaultTemp) } returns true

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(EnvironmentVariablesImpl.defaultTemp.path)) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @Test(dataProvider = "osTypesData")
    fun shouldOverrideTeamCityTempByCustomTeamCityTempWhenNotVirtualAndNotWindowsAndLenghtLessMore60AndTmpExists(os: OSType) {
        // Given
        val environmentVariables = createInstance()
        val systemPath = File("system")
        val tempPath = "a".repeat(61)

        // When
        every { _environment.os } returns os
        every { _environment.tryGetVariable("HOME") } returns "path"
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns os
        every { _pathsService.getPath(PathType.BuildTemp) } returns File(tempPath)
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.defaultTemp) } returns false
        every { _fileSystemService.isExists(EnvironmentVariablesImpl.customTeamCityTemp) } returns false
        every { _fileSystemService.createDirectory(EnvironmentVariablesImpl.customTeamCityTemp) } returns true

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        for (envVar in EnvironmentVariablesImpl.getTempDirVariables(EnvironmentVariablesImpl.customTeamCityTemp.canonicalPath)) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    private fun createInstance() = EnvironmentVariablesImpl(_environment, _pathsService, _fileSystemService, _virtualContext)
}