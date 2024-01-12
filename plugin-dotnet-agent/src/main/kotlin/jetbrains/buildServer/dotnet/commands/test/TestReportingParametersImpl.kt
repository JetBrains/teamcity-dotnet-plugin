

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TestReportingMode
import java.util.*

class TestReportingParametersImpl(
        private val _parametersService: ParametersService)
    : TestReportingParameters {
    override fun getMode(context: DotnetCommandContext): EnumSet<TestReportingMode> {
        val modes = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING)?.let {
            TestReportingMode.parse(it)
        } ?: EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)

        if (!modes.isEmpty()) {
            return modes
        }

        val modeSet = mutableSetOf(TestReportingMode.On)
        if (context.toolVersion >= Version.MultiAdapterPath_5_0_103_Version) {
            modeSet.add(TestReportingMode.MultiAdapterPath_5_0_103)
        }
        else {
            if (context.toolVersion >= Version.MultiAdapterPathVersion) {
                modeSet.add(TestReportingMode.MultiAdapterPath)
            }
        }

        return EnumSet.copyOf<TestReportingMode>(modeSet)
    }
}