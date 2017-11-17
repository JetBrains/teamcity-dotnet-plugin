package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class TestReportingParametersImpl(private val _parametersService: ParametersService)
    : TestReportingParameters {
    override val mode: TestReportingMode
        get() = TestReportingMode.tryParse(_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING) ?: "") ?: TestReportingMode.On
}