package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.EnvironmentVariables

internal class TestEnvironmentVariables(
    private val _parametersService: ParametersService,
    private val _runnerScopedTestEnvironmentBuilder: BuildStepScopedTestEnvironmentBuilder
) : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        val fallbackToStdOutTestReporting = TestReportingViaFileStreamingHelper.shouldFallbackToStdOutTestReporting(_parametersService)

        if (fallbackToStdOutTestReporting) {
            yield(CommandLineEnvironmentVariable(FALLBACK_TO_STDOUT_TEST_REPORTING_ENV_VAR, "true"))
        } else {
            val testReportsFilesPath = _runnerScopedTestEnvironmentBuilder.getTestReportsFilesPathForBuildStep().toString()
            yield(CommandLineEnvironmentVariable(TEAMCITY_TEST_REPORT_FILES_PATH_ENV_VAR, testReportsFilesPath))
        }
    }

    private companion object {
        const val FALLBACK_TO_STDOUT_TEST_REPORTING_ENV_VAR = "TEAMCITY_FALLBACK_TO_STDOUT_TEST_REPORTING"
        const val TEAMCITY_TEST_REPORT_FILES_PATH_ENV_VAR = "TEAMCITY_TEST_REPORT_FILES_PATH"
    }
}
