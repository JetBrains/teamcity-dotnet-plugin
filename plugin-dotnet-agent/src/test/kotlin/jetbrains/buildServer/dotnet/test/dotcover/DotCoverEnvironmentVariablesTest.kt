package jetbrains.buildServer.dotnet.test.dotcover

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotcover.DotCoverEnvironmentVariables
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import kotlin.random.Random

class DotCoverEnvironmentVariablesTest {
    @MockK private lateinit var  _virtualContext: VirtualContext;
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_OVERRIDING_TEMP_DIR_WITH_EMPTY_VALUE_ENABLED) } returns "true"
    }

    @DataProvider(name = "defaultVars")
    fun defaultVars() = arrayOf(
        arrayOf(OSType.UNIX, DotCoverEnvironmentVariables.linuxDefaultVariables),
        arrayOf(OSType.MAC, emptySequence<CommandLineEnvironmentVariable>()),
        arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>())
    )

    @Test(dataProvider = "defaultVars")
    fun `should provide Linux default vars in Linux container`(containerOS: OSType, expectedVariables: Sequence<CommandLineEnvironmentVariable>) {
        // arrange
        every { _virtualContext.targetOSType } answers { containerOS }
        every { _environment.os } answers { mockk() }
        every { _virtualContext.isVirtual } answers { true }
        val pathMock = mockk<File>().also { every {  it.path } answers { "" } }
        every { _pathsService.getPath(any()) } answers { pathMock }
        every { _fileSystemService.isExists(any()) } answers { true }
        every { _fileSystemService.isDirectory(any()) } answers { true }
        val environmentVariables = createInstance()

        // act
        val actualVariables = environmentVariables.getVariables().toList()

        // assert
        Assert.assertEquals(actualVariables.size, expectedVariables.count())
        for (envVar in expectedVariables.toList()) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @DataProvider(name = "containersOnWindowsHostVars")
    fun containersOnWindowsHostVars() = arrayOf(
        arrayOf(OSType.UNIX, DotCoverEnvironmentVariables.linuxDefaultVariables + DotCoverEnvironmentVariables.getTempDirVariables()),
        arrayOf(OSType.MAC, DotCoverEnvironmentVariables.getTempDirVariables()),
        arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>())
    )

    @Test(dataProvider = "containersOnWindowsHostVars")
    fun `should provide temp dir env vars in container on Windows host`(containerOS: OSType, expectedVars: Sequence<CommandLineEnvironmentVariable>) {
        // arrange
        every { _virtualContext.isVirtual } answers { true }
        every { _virtualContext.targetOSType } answers { containerOS }
        every { _environment.os } answers { OSType.WINDOWS }
        val environmentVariables = createInstance()

        // act
        val actualVariables = environmentVariables.getVariables().toList()

        // assert
        Assert.assertEquals(actualVariables.size, expectedVars.count())
        for (envVar in expectedVars.toList()) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @DataProvider(name = "containersOnNonWindowsHostAndShortTempDirPathVars")
    fun containersOnNonWindowsHostAndShortTempDirPathVars() = arrayOf(
        arrayOf(OSType.UNIX, DotCoverEnvironmentVariables.linuxDefaultVariables),
        arrayOf(OSType.MAC, emptySequence<CommandLineEnvironmentVariable>()),
        arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>())
    )

    @Test(dataProvider = "containersOnNonWindowsHostAndShortTempDirPathVars")
    fun `should provide temp dir env vars in container and on non-Windows host with a short temp directory path`(containerOS: OSType, expectedVars: Sequence<CommandLineEnvironmentVariable>) {
        // arrange
        every { _virtualContext.isVirtual } answers { true }
        every { _virtualContext.targetOSType } answers { containerOS }
        every { _environment.os } answers { OSType.MAC }
        val pathMock = mockk<File>().also { every {  it.path } answers { genRandomString(60) } }
        every { _pathsService.getPath(any()) } answers { pathMock }
        every { _fileSystemService.isExists(any()) } answers { true }
        every { _fileSystemService.isDirectory(any()) } answers { true }
        val environmentVariables = createInstance()

        // act
        val actualVariables = environmentVariables.getVariables().toList()

        // assert
        Assert.assertEquals(actualVariables.size, expectedVars.count())
        for (envVar in expectedVars.toList()) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @DataProvider(name = "containersOnNonWindowsHostAndLongTempDirPathVars")
    fun containersOnNonWindowsHostAndLongTempDirPathVars() = arrayOf(
        arrayOf(OSType.UNIX, DotCoverEnvironmentVariables.linuxDefaultVariables + DotCoverEnvironmentVariables.getTempDirVariables(DotCoverEnvironmentVariables.defaultTemp.path)),
        arrayOf(OSType.MAC, DotCoverEnvironmentVariables.getTempDirVariables(DotCoverEnvironmentVariables.defaultTemp.path)),
        arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>())
    )

    @Test(dataProvider = "containersOnNonWindowsHostAndLongTempDirPathVars")
    fun `should provide temp dir env vars in container and on non-Windows host with a long temp directory path`(containerOS: OSType, expectedVars: Sequence<CommandLineEnvironmentVariable>) {
        // arrange
        every { _virtualContext.isVirtual } answers { true }
        every { _virtualContext.targetOSType } answers { containerOS }
        every { _environment.os } answers { OSType.MAC }
        val pathMock = mockk<File>().also { every {  it.path } answers { genRandomString(61) } }
        every { _pathsService.getPath(any()) } answers { pathMock }
        every { _fileSystemService.isExists(any()) } answers { true }
        every { _fileSystemService.isDirectory(any()) } answers { true }
        val environmentVariables = createInstance()

        // act
        val actualVariables = environmentVariables.getVariables().toList()

        // assert
        Assert.assertEquals(actualVariables.size, expectedVars.count())
        for (envVar in expectedVars.toList()) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    @DataProvider(name = "containers on non-Windows host and long temp dir path without default temp dir")
    fun containersOnNonWindowsHostAndLongTempDirPathWithoutDefaultTempDirVars() = arrayOf(
        arrayOf(OSType.UNIX, DotCoverEnvironmentVariables.linuxDefaultVariables + DotCoverEnvironmentVariables.getTempDirVariables(DotCoverEnvironmentVariables.customTeamCityTemp.path)),
        arrayOf(OSType.MAC, DotCoverEnvironmentVariables.getTempDirVariables(DotCoverEnvironmentVariables.customTeamCityTemp.path)),
        arrayOf(OSType.WINDOWS, emptySequence<CommandLineEnvironmentVariable>())
    )

    @Test(dataProvider = "containers on non-Windows host and long temp dir path without default temp dir")
    fun `should provide temp dir env vars in container and on non-Windows host with a long temp directory path without default temp dir`(containerOS: OSType, expectedVars: Sequence<CommandLineEnvironmentVariable>) {
        // arrange
        every { _virtualContext.isVirtual } answers { true }
        every { _virtualContext.targetOSType } answers { containerOS }
        every { _environment.os } answers { OSType.MAC }
        val pathMock = mockk<File>().also { every {  it.path } answers { genRandomString(61) } }
        every { _pathsService.getPath(any()) } answers { pathMock }
        every { _fileSystemService.isExists(DotCoverEnvironmentVariables.defaultTemp) } answers { false }
        every { _fileSystemService.isExists(DotCoverEnvironmentVariables.customTeamCityTemp) } answers { true }
        every { _fileSystemService.isDirectory(any()) } answers { true }
        val environmentVariables = createInstance()

        // act
        val actualVariables = environmentVariables.getVariables().toList()

        // assert
        Assert.assertEquals(actualVariables.size, expectedVars.count())
        for (envVar in expectedVars.toList()) {
            Assert.assertTrue(actualVariables.contains(envVar))
        }
    }

    private fun createInstance() = DotCoverEnvironmentVariables(
        _environment,
        _virtualContext,
        _fileSystemService,
        _pathsService,
        _parametersService
    )

    private fun genRandomString(length: Int = 10): String {
        val charPool = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_/"
        return (1..length)
            .map { Random.nextInt(0, charPool.length).let { charPool[it] } }
            .joinToString("")
    }
}