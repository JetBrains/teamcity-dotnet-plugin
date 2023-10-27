package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants

class TestsSplittingModeProviderImpl(
    private val _parametersService: ParametersService,
    private val _testsSplittingSettings: TestsSplittingSettings,
) : TestsSplittingModeProvider {
    override fun getMode(dotnetVersion: Version): TestsSplittingMode =
        when {
            !isEnabled -> TestsSplittingMode.Disabled
            useTestNameFilter -> TestsSplittingMode.TestNameFilter
            shouldUseSuppression(dotnetVersion) -> TestsSplittingMode.Suppression
            else -> TestsSplittingMode.TestClassNameFilter
        }

    private fun shouldUseSuppression(dotnetVersion: Version): Boolean =
        useTestSuppression
            && dotnetVersion >= Version.MinDotnetVersionForTestSuppressor
            && _testsSplittingSettings.hasEnoughTestClassesToActivateSuppression

    private val isEnabled: Boolean get() = _testsSplittingSettings.testsClassesFilePath != null

    private val useTestSuppression get() =
        getBoolConfigurationParameter(DotnetConstants.PARAM_PARALLEL_TESTS_USE_SUPPRESSION, defaultValue = true)

    private val useTestNameFilter get() =
        getBoolConfigurationParameter(DotnetConstants.PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER)

    private fun getBoolConfigurationParameter(paramName: String, defaultValue: Boolean = false) =
        _parametersService
            .tryGetParameter(ParameterType.Configuration, paramName)
            ?.trim()
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: defaultValue
}