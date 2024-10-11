package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.impl.operationModes.AgentOperationModeHolder
import jetbrains.buildServer.agent.impl.operationModes.ExecutorStartMode
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.common.MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import jetbrains.buildServer.util.OSType
import org.testng.Assert.*
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetEnvironmentVariablesTest {
    @MockK private lateinit var _environment: Environment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _pathsService: PathsService
    @MockK private lateinit var _loggerResolver: LoggerResolver
    @MockK private lateinit var _agentOperationModeHolder: AgentOperationModeHolder
    private lateinit var _additionalEnvVars: MutableList<EnvironmentVariables>
    private lateinit var _environmentVariables: EnvironmentVariables

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this, )
        clearAllMocks()
        _environment = mockk(relaxed = true)
        _parametersService = mockk(relaxed = true)
        _pathsService = mockk(relaxed = true)
        _loggerResolver = mockk(relaxed = true)
        _agentOperationModeHolder = mockk(relaxed = true)
        _additionalEnvVars = mutableListOf()
        _environmentVariables = DotnetEnvironmentVariables(
            _environment, _parametersService, _pathsService, _additionalEnvVars, _loggerResolver, _agentOperationModeHolder)
    }

    @DataProvider(name = "common env vars")
    fun `common env vars`(): Array<Array<Any>> = arrayOf(
        arrayOf(CommonEnvVarTestCase("DOTNET_CLI_TELEMETRY_OPTOUT", "true")),
        arrayOf(CommonEnvVarTestCase("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true")),
        arrayOf(CommonEnvVarTestCase("NUGET_XMLDOC_MODE", "skip")),
        arrayOf(CommonEnvVarTestCase("TEAMCITY_MSBUILD_LOGGER", "/path/to/msbuild/logger") {
            every { _loggerResolver.resolve(ToolType.MSBuild) } returns mockk<File>().also { f ->
                every { f.canonicalPath } returns "/path/to/msbuild/logger"
            }
        }),
        arrayOf(CommonEnvVarTestCase("TEAMCITY_VSTEST_LOGGER", "/path/to/vstest/logger") {
            every { _loggerResolver.resolve(ToolType.VSTest) } returns mockk<File>().also { f ->
                every { f.canonicalPath } returns "/path/to/vstest/logger"
            }
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","true") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "true"
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","true") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "  TrUe   "
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","false") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "false"
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","false") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "1"
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","false") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "0"
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","false") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns "     "
        }),
        arrayOf(CommonEnvVarTestCase("UseSharedCompilation","false") {
            every { _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
            } returns null
        }),
    )

    data class CommonEnvVarTestCase(
        val name: String,
        val value: String,
        val arrange: () -> Unit = {},
    )

    @Test(dataProvider = "common env vars")
    fun `should always provide common environment variable`(testCase: CommonEnvVarTestCase) {
        // arrange
        testCase.arrange()

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertNotNull(result.singleOrNull { it.name == testCase.name && it.value == testCase.value })
    }

    @Test
    fun `should always provide exact number of always present environment variable`() {
        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertEquals(result.count(), 7)
    }

    @DataProvider(name = "OS and home env var name")
    fun `OS and home env var name`(): Array<Array<Any>> = arrayOf(
        arrayOf(OSType.WINDOWS, "USERPROFILE"),
        arrayOf(OSType.UNIX, "HOME"),
        arrayOf(OSType.MAC, "HOME"),
    )

    @Test(dataProvider = "OS and home env var name")
    fun `should not provide user home env var if already defined by a user`(os: OSType, userHomeEnvVarName: String) {
        // arrange
        _environment.also { e ->
            every { e.os } returns os
            every { e.tryGetVariable(userHomeEnvVarName) } returns "/user/defined/path/to/user/home"
        }

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertFalse(result.any { it.name == userHomeEnvVarName })
    }

    @Test(dataProvider = "OS and home env var name")
    fun `should provide user home env var from Java system property if not defined by a user`(os: OSType, userHomeEnvVarName: String) {
        // arrange
        _environment.also { e ->
            every { e.os } returns os
            every { e.tryGetVariable(userHomeEnvVarName) } returns null
        }

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertNotNull(result.singleOrNull { it.name == userHomeEnvVarName && it.value == System.getProperty("user.home") })
    }

    @DataProvider(name = ".NET SDK version and result")
    fun `dotnet SDK version and result`(): Array<Array<Any>> = arrayOf(
        arrayOf(Version(2, 2), true),
        arrayOf(Version(3, 1, 0, "rc"), true),
        arrayOf(Version(7, 0 ,400), true),
        arrayOf(Version(8, 0 ,100, "rc"), false),
        arrayOf(Version(8, 0 ,100, "preview2"), false),
        arrayOf(Version(8, 0 ,0), false),
        arrayOf(Version(8, 0 ,100), false),
    )

    @Test(dataProvider = ".NET SDK version and result")
    fun `should provide COMPlus_EnableDiagnostics == 0 env var when dotnet SDK version less than dotnet 8`(sdkVersion: Version, expectedResult: Boolean) {
        // act
        val result = _environmentVariables.getVariables(sdkVersion)

        // assert
        assertEquals(result.any { it.name == "COMPlus_EnableDiagnostics" && it.value == "0" }, expectedResult)
    }

    @DataProvider(name = "messages guard parameter and result")
    fun `messages guard parameter and result`(): Array<Array<Any?>> = arrayOf(
        arrayOf("", false),
        arrayOf("  ", false),
        arrayOf("1", false),
        arrayOf("0", false),
        arrayOf("  tRuE ", true),
        arrayOf(" FalSE  ", false),
        arrayOf("true", true),
        arrayOf("false", false),
        arrayOf(null, false),
    )

    @Test(dataProvider = "messages guard parameter and result")
    fun `should provide TEAMCITY_SERVICE_MESSAGES_PATH env var with agent temp directory when parameter is set`(parameterValue: String?, expectedResult: Boolean) {
        // arrange
        every { _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD) } returns parameterValue
        val agentTempPath = "/path/to/agent/temp"
        every { _pathsService.getPath(PathType.AgentTemp) } returns mockk<File>().also { f ->
            every { f.canonicalPath } returns agentTempPath
        }

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertEquals(result.any { it.name == "TEAMCITY_SERVICE_MESSAGES_PATH" && it.value == agentTempPath }, expectedResult)
    }

    @Test
    fun `should provide additional env vars`() {
        // arrange
        _additionalEnvVars.addAll(listOf(
            mockk {
                every { getVariables(any()) } returns sequenceOf(
                    mockk {
                        every { name } returns "VAR1"
                        every { value } returns "VAL1"
                    },
                    mockk {
                        every { name } returns "VAR2"
                        every { value } returns "VAL2"
                    }
                )
            },
            mockk {
                every { getVariables(any()) } returns sequenceOf(
                    mockk {
                        every { name } returns "VAR3"
                        every { value } returns "VAL3"
                    },
                )
            },
        ))

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertTrue(result.any { it.name == "VAR1" && it.value == "VAL1" })
        assertTrue(result.any { it.name == "VAR2" && it.value == "VAL2" })
        assertTrue(result.any { it.name == "VAR3" && it.value == "VAL3" })
        assertEquals(result.count(), 10)
    }

    @Test
    fun `should modify cli home for executors`(){
        // arrange
        every {
            _agentOperationModeHolder.operationMode
        } returns mockk<ExecutorStartMode>(relaxed = true)

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertTrue(result.any { it.name == DotnetEnvironmentVariables.dotNetCliHome })
    }

    @Test
    fun `shouldn't modify cli home for executors if it has already been set`(){
        // arrange
        every {
            _agentOperationModeHolder.operationMode
        } returns mockk<ExecutorStartMode>(relaxed = true)
        every {
            _environment.tryGetVariable(DotnetEnvironmentVariables.dotNetCliHome) } returns "customCliHome"

        // act
        val result = _environmentVariables.getVariables(mockk<Version>(relaxed = true))

        // assert
        assertTrue(result.none { it.name == DotnetEnvironmentVariables.dotNetCliHome })
    }
}
