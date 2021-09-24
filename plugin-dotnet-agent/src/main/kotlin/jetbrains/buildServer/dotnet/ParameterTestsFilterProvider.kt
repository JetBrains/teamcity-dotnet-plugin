package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class ParameterTestsFilterProvider(
        private val _parametersService: ParametersService)
    : TestsFilterProvider {
    override val filterExpression: String
        get() = (_parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_CASE_FILTER) ?: "").trim()
}