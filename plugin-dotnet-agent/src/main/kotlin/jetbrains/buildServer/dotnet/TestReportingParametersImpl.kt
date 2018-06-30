package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.util.*

class TestReportingParametersImpl(
        private val _parametersService: ParametersService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo)
    : TestReportingParameters {
    override val mode: EnumSet<TestReportingMode>
        get() {
            val modes = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_TEST_REPORTING)?.let {
                TestReportingMode.parse(it)
            } ?: EnumSet.noneOf<TestReportingMode>(TestReportingMode::class.java)

            if (!modes.isEmpty()) {
                return modes
            }

            val modeSet = mutableSetOf(TestReportingMode.On)
            if (_dotnetCliToolInfo.version >= MultiAdapterPathVersion) {
                modeSet.add(TestReportingMode.MultiAdapterPath)
            }

            return EnumSet.copyOf<TestReportingMode>(modeSet)
        }

    companion object {
        val MultiAdapterPathVersion: Version = Version(2, 1, 102)
    }
}