

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.util.StringUtil

class ParameterTestsFilterProvider(
        private val _parametersService: ParametersService)
    : TestsFilterProvider {
    override fun getFilterExpression(mode: TestsSplittingMode): String =
        StringUtil.unquoteString((_parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_CASE_FILTER) ?: "").trim()).trim()
}