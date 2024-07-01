package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.ReduceTestFailureFeedbackParameters
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_APP_CONFIG_FILE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_CATEGORY_EXCLUDE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_CATEGORY_INCLUDE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_COMMAND_LINE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_PATH
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_TESTS_FILES_EXCLUDE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_TESTS_FILES_INCLUDE
import jetbrains.buildServer.nunit.NUnitRunnerConstants.NUNIT_USES_PROJECT_FILE
import jetbrains.buildServer.nunit.testReordering.RunnerConfigVarTestInfoParser
import jetbrains.buildServer.nunit.testReordering.TestInfo

class NUnitSettingsImpl(
    private val _parametersService: ParametersService,
    private val _runnerConfigVarTestInfoParser: RunnerConfigVarTestInfoParser
) : NUnitSettings {
    override val testReorderingEnabled: Boolean
        get() = getRunnerParameter(ReduceTestFailureFeedbackParameters.RUN_RISK_GROUP_TESTS_FIRST_PARAM).isNullOrBlank().not()

    override val testReorderingRecentlyFailedTests: List<TestInfo>
        get() {
            val str = getRunnerParameter(ReduceTestFailureFeedbackParameters.RECENTLY_FAILED_TESTS_PARAM)
            return when {
                !str.isNullOrEmpty() -> _runnerConfigVarTestInfoParser.parse(str)
                else -> emptyList()
            }
        }

    override val appConfigFile: String?
        get() = getRunnerParameter(NUNIT_APP_CONFIG_FILE)

    override val additionalCommandLine: String?
        get() = getRunnerParameter(NUNIT_COMMAND_LINE)

    override val includeCategories: String?
        get() = getRunnerParameter(NUNIT_CATEGORY_INCLUDE)

    override val excludeCategories: String?
        get() = getRunnerParameter(NUNIT_CATEGORY_EXCLUDE)

    override val includeTestFiles: String
        get() = getRunnerParameter(NUNIT_TESTS_FILES_INCLUDE) ?: ""

    override val excludeTestFiles: String
        get() = getRunnerParameter(NUNIT_TESTS_FILES_EXCLUDE) ?: ""

    override val nUnitPath: String?
        get() = getRunnerParameter(NUNIT_PATH)

    override val useProjectFile: Boolean
        get() = getConfigurationParameter(NUNIT_USES_PROJECT_FILE)?.let { it.toBoolean() } ?: false

    private fun getRunnerParameter(parameterName: String) =
        _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    @Suppress("SameParameterValue")
    private fun getConfigurationParameter(parameterName: String) =
        _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)
}
