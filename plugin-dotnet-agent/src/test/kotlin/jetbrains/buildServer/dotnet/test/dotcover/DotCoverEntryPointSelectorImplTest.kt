import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelectorImpl
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelectorImpl.Companion.DOTCOVER_REQUIREMENTS_BUILD_PROBLEM
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_CORE_RUNTIME
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_SUFFIX_PATH
import jetbrains.buildServer.dotnet.test.StringExtensions.toPlatformPath
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.Assert.assertThrows
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotCoverEntryPointSelectorImplTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _fileSystemService: FileSystemService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _selector: DotCoverEntryPointSelectorImpl

    @BeforeMethod
    fun setUp() {
        _parametersService = mockk(relaxed = true) // relaxed allows methods to return default "mocked" values
        _virtualContext = mockk(relaxed = true)
        _fileSystemService = mockk(relaxed = true)
        _loggerService = mockk(relaxed = true)

        _selector = DotCoverEntryPointSelectorImpl(_parametersService, _virtualContext, _fileSystemService, _loggerService)
    }

    @DataProvider(name = "OS and entry point file name")
    fun `OS and entry point file name`(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(OSType.WINDOWS, "dotCover.exe"),
            arrayOf(OSType.UNIX, "dotCover.dll"),
            arrayOf(OSType.MAC, "dotCover.dll"),
        )
    }

    @Test(dataProvider = "OS and entry point file name")
    fun `should return entry point and skip validation of agent parameters when build run in a container`(os: OSType, entryPointFileName: String) {
        // arrange
        val dotCoverHomePath = "/path/to/dotCover/home".toPlatformPath()
        val dotCoverEntryPointPath = "$dotCoverHomePath/$entryPointFileName".toPlatformPath()
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverHomePath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os
        every { _fileSystemService.isExists(match { it.path == dotCoverEntryPointPath }) } returns true

        // act
        val result = _selector.select()

        // assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(result.getOrNull()?.path, dotCoverEntryPointPath)
        verify (exactly = 0) { _parametersService.getParameterNames(ParameterType.Configuration) }
    }

    @Test(dataProvider = "OS and entry point file name")
    fun `should return entry point and skip validation of agent parameters when skip validation flag has been explicitly set`(os: OSType, entryPointFileName: String) {
        // arrange
        val dotCoverHomePath = "/path/to/dotCover/home".toPlatformPath()
        val dotCoverEntryPointPath = "$dotCoverHomePath/$entryPointFileName".toPlatformPath()
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverHomePath
        every { _virtualContext.isVirtual } returns true
        every { _virtualContext.targetOSType } returns os
        every { _fileSystemService.isExists(match { it.path == dotCoverEntryPointPath }) } returns true

        // act
        val result = _selector.select(skipRequirementsValidation = true)

        // assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(result.getOrNull()?.path, dotCoverEntryPointPath)
        verify (exactly = 0) { _parametersService.getParameterNames(ParameterType.Configuration) }
    }

    @DataProvider(name = "OS, entry point file name, agent parameters names and expected compatibility of agent with entry point")
    fun `OS, entry point file name, agent parameters names and expected compatibility of agent with entry point`(): Array<Array<Any>> {
        return arrayOf(
            // modern cross-platform dotCover package on Windows with installed .NET Framework 4.7.2
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.WINDOWS,
                existingEntryPointFileNames = sequenceOf("dotCover.exe", "dotCover.dll"),
                selectingEntryPointFileName = "dotCover.exe",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_DOTNET_FAMEWORK}4.7.2${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on Windows with installed .NET Framework 4.8
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.WINDOWS,
                existingEntryPointFileNames = sequenceOf("dotCover.exe", "dotCover.dll"),
                selectingEntryPointFileName = "dotCover.exe",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_DOTNET_FAMEWORK}4.8${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on Windows with installed .NET Framework 4.6.1
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.WINDOWS,
                existingEntryPointFileNames = sequenceOf("dotCover.exe", "dotCover.dll"),
                selectingEntryPointFileName = "dotCover.exe",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_DOTNET_FAMEWORK}4.6.1${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = false,
            )),

            // modern cross-platform dotCover package on Linux/Unix with installed .NET Core 3.1
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.UNIX,
                existingEntryPointFileNames =  sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}3.1${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on Linux/Unix with installed .NET 7
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.UNIX,
                existingEntryPointFileNames =  sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}7.0.403${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on Linux/Unix with installed .NET 8-preview1
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.UNIX,
                existingEntryPointFileNames =  sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}8-preview1${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on Linux/Unix with installed .NET Core 3.0
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.UNIX,
                existingEntryPointFileNames =  sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}3.0${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = false,
            )),

            // modern cross-platform dotCover package on MacOS with installed .NET Core 3.1
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.MAC,
                existingEntryPointFileNames = sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}3.1${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = true,
            )),

            // modern cross-platform dotCover package on MacOS with installed .NET Core 3.0
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.MAC,
                existingEntryPointFileNames = sequenceOf("dotCover.dll"),
                selectingEntryPointFileName = "dotCover.dll",
                agentParametersNames = sequenceOf(
                    "${CONFIG_PREFIX_CORE_RUNTIME}3.0${CONFIG_SUFFIX_PATH}"
                ),
                shouldValidateCompatibility = true,
                expectedCompatibility = false,
            )),

            // old Windows-only dotCover package on Windows
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.WINDOWS,
                existingEntryPointFileNames = sequenceOf("dotCover.exe"),
                selectingEntryPointFileName = "dotCover.exe",
                agentParametersNames = emptySequence(),
                shouldValidateCompatibility = false,
                expectedCompatibility = true,
            )),

            // deprecated cross-platform dotCover package on Linux/Unix
            arrayOf(AgentCompatibilityWithDotCover(
                os = OSType.UNIX,
                existingEntryPointFileNames =  sequenceOf("dotCover.sh", "dotCover.exe"),
                selectingEntryPointFileName = "dotCover.sh",
                agentParametersNames = emptySequence(),
                shouldValidateCompatibility = false,
                expectedCompatibility = true,
            )),
        )
    }

    data class AgentCompatibilityWithDotCover(
        val os: OSType,
        val existingEntryPointFileNames: Sequence<String>,
        val selectingEntryPointFileName: String,
        val agentParametersNames: Sequence<String>,
        val shouldValidateCompatibility: Boolean,
        val expectedCompatibility: Boolean,
    )

    @Test(dataProvider = "OS, entry point file name, agent parameters names and expected compatibility of agent with entry point")
    fun `should return entry point and validate agent parameters when build run not in a container`(data: AgentCompatibilityWithDotCover) {
        // arrange
        val dotCoverHomePath = "/path/to/dotCover/home".toPlatformPath()
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns data.os
        every { _fileSystemService.isExists(match { data.existingEntryPointFileNames.contains(it.name) }) } returns true
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverHomePath
        every { _parametersService.getParameterNames(ParameterType.Configuration) } answers { data.agentParametersNames }

        // act
        val result = _selector.select()

        // assert
        when (data.expectedCompatibility) {
            true -> {
                Assert.assertTrue(result.isSuccess)
                Assert.assertEquals(result.getOrNull()?.path, "$dotCoverHomePath/${data.selectingEntryPointFileName}".toPlatformPath())
                verify (exactly = if (data.shouldValidateCompatibility) 1 else 0) {
                    _parametersService.getParameterNames(ParameterType.Configuration)
                }
            }
            false -> {
                Assert.assertTrue(result.isFailure)
                verify (exactly = if (data.shouldValidateCompatibility) 1 else 0) {
                    _parametersService.getParameterNames(ParameterType.Configuration)
                }
                verify (exactly = 1) { _loggerService.writeBuildProblem(DOTCOVER_REQUIREMENTS_BUILD_PROBLEM, eq(BuildProblemData.TC_ERROR_MESSAGE_TYPE), any()) }
            }
        }
    }

    @DataProvider(name = "dotCover home directory")
    fun `dotCover home directory`(): Array<Array<Any?>> {
        return arrayOf(arrayOf(""), arrayOf(" "), arrayOf(null))
    }

    @Test(dataProvider = "dotCover home directory")
    fun `should log error when dotCover home path is invalid`(dotCoverPath: String?) {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns dotCoverPath

        // act, assert
        assertThrows(ToolCannotBeFoundException::class.java) { _selector.select() }
    }

    @Test
    fun `should throw ToolCannotBeFoundException for non-Windows systems when no proper entry point is found`() {
        // arrange
        every { _virtualContext.isVirtual } returns false
        every { _virtualContext.targetOSType } returns OSType.UNIX // or OSType.MAC for MacOS
        every { _fileSystemService.isExists(any()) } returns false // simulate missing files
        every { _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME) } returns "invalidNonWindowsPath"

        // act, assert
        assertThrows(ToolCannotBeFoundException::class.java) { _selector.select() }
    }
}
