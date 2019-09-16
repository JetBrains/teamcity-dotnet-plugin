package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.util.*

class TestReportingParametersImpl(
        private val _parametersService: ParametersService)
    : TestReportingParameters {
    override fun getMode(context: DotnetBuildContext): EnumSet<TestReportingMode> {
        val modes = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING)?.let {
            TestReportingMode.parse(it)
        } ?: EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)

        if (!modes.isEmpty()) {
            return modes
        }

        val modeSet = mutableSetOf(TestReportingMode.On)
        if (context.toolVersion >= Version.MultiAdapterPathVersion) {
            modeSet.add(TestReportingMode.MultiAdapterPath)
        }
        return EnumSet.copyOf<TestReportingMode>(modeSet)
    }
}