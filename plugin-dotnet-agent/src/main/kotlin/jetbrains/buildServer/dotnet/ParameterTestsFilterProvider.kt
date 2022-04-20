package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.StringUtil

class ParameterTestsFilterProvider(
        private val _parametersService: ParametersService)
    : TestsFilterProvider {
    override val filterExpression: String
        get() = StringUtil.unquoteString((_parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_CASE_FILTER) ?: "").trim()).trim()
}