package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.TargetRegistry
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class EnvironmentVariablesTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _sharedCompilation: SharedCompilation
    @MockK private lateinit var _pathsService: PathsService
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
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation, _pathsService, _virtualContext)
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _sharedCompilation.requireSuppressing(Version(1, 2, 3)) } returns false
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath))).toList())
    }

    @Test
    fun shouldUseSharedCompilation() {
        // Given
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation, _pathsService, _virtualContext)
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _sharedCompilation.requireSuppressing(Version(1, 2, 3)) } returns true
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns false

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath)) + sequenceOf(EnvironmentVariablesImpl.useSharedCompilationEnvironmentVariable)).toList())
    }

    @DataProvider(name = "osTypesData")
    fun osTypesData(): Array<Array<OSType>> {
        return arrayOf(
                arrayOf(OSType.UNIX),
                arrayOf(OSType.MAC))
    }

    @Test(dataProvider = "osTypesData")
    fun shouldProvideDefaultVarsWhenVirtualContext(os: OSType) {
        // Given
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation, _pathsService, _virtualContext)
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _sharedCompilation.requireSuppressing(Version(1, 2, 3)) } returns false
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath)) + EnvironmentVariablesImpl.tempDirVariables).toList())
    }

    @Test
    fun shouldProvideDefaultVarsWhenVirtualContextForWindowsContainer() {
        // Given
        val environmentVariables = EnvironmentVariablesImpl(_environment, _sharedCompilation, _pathsService, _virtualContext)
        val systemPath = File("system")
        val nugetPath = File(File(systemPath, "dotnet"), ".nuget").absolutePath

        // When
        every { _environment.os } returns OSType.WINDOWS
        every { _environment.tryGetVariable("USERPROFILE") } returns "path"
        every { _sharedCompilation.requireSuppressing(Version(1, 2, 3)) } returns false
        every { _pathsService.getPath(PathType.System) } returns systemPath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns OSType.WINDOWS

        val actualVariables = environmentVariables.getVariables(Version(1, 2, 3)).toList()

        // Then
        Assert.assertEquals(actualVariables, (EnvironmentVariablesImpl.defaultVariables + sequenceOf(CommandLineEnvironmentVariable("NUGET_PACKAGES", "v_" + nugetPath))).toList())
    }
}