package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import io.mockk.every
import io.mockk.mockk
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.BuildStepScopedTestEnvironmentBuilder
import jetbrains.buildServer.dotnet.commands.test.TestEnvironmentVariables
import org.testng.Assert
import org.testng.annotations.Test
import kotlin.io.path.Path

class TestEnvironmentVariablesTest {
    @Test
    fun `should only set test reporting fallback environment variable if fallback to stdout test reporting is enabled`() {
        // Given
        val parametersService = mockk<ParametersService>()
        every { parametersService.tryGetParameter(any(), DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING) } returns "true"
        val runnerScopedTestEnvironmentBuilder = mockk<BuildStepScopedTestEnvironmentBuilder>()
        val version = mockk<Version>()
        val environmentVariables = TestEnvironmentVariables(parametersService, runnerScopedTestEnvironmentBuilder)

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
        val runnerScopedTestEnvironmentBuilder = mockk<BuildStepScopedTestEnvironmentBuilder>()
        val expectedPath = "path-for-test-reports-files"
        every { runnerScopedTestEnvironmentBuilder.getTestReportsFilesPathForBuildStep() } returns Path(expectedPath)
        val version = mockk<Version>()
        val environmentVariables = TestEnvironmentVariables(parametersService, runnerScopedTestEnvironmentBuilder)

        // When
        val variables = environmentVariables.getVariables(version).toList()

        // Then
        Assert.assertEquals(variables.size, 1)
        Assert.assertEquals(variables.first().name, "TEAMCITY_TEST_REPORT_FILES_PATH")
        Assert.assertEquals(variables.first().value, expectedPath)
    }
}
