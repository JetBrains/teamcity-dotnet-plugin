package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.TestEnvironmentVariables
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class TestEnvironmentVariablesTest {
    @Test
    fun `should only set test reporting fallback environment variable if fallback to stdout test reporting is enabled`() {
        // Given
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "true"
        val pathsService = mockk<PathsService>()
        val version = mockk<Version>()
        val environmentVariables = TestEnvironmentVariables(parametersService, pathsService)

        // When
        val variables = environmentVariables.getVariables(version).toList()

        // Then
        Assert.assertEquals(variables.size, 1)
        Assert.assertEquals(variables.first().name, "TEAMCITY_FALLBACK_TO_STDOUT_TEST_REPORTING")
        Assert.assertEquals(variables.first().value, "true")
    }

    @Test
    fun `should only set test reporting file path environment variable if fallback to stdout test reporting is not enabled`() {
        // Given
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns null
        val pathsService = mockk<PathsService>()
        every { pathsService.getPath(PathType.AgentTemp) } returns File("/agentTmp")
        val version = mockk<Version>()
        val environmentVariables = TestEnvironmentVariables(parametersService, pathsService)

        // When
        val variables = environmentVariables.getVariables(version).toList()

        // Then
        Assert.assertEquals(variables.size, 1)
        Assert.assertEquals(variables.first().name, "TEAMCITY_TEST_REPORT_FILES_PATH")
        Assert.assertEquals(variables.first().value, "/agentTmp/TestReports")
    }
}
