package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants

internal object TestReportingViaFileStreamingHelper {
    fun shouldFallbackToStdOutTestReporting(parametersService: ParametersService): Boolean {
        val fallbackToStdOutTestReportingValue = parametersService
            .tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_USE_STDOUT_TEST_REPORTING)
        return fallbackToStdOutTestReportingValue.toBoolean()
    }
}
