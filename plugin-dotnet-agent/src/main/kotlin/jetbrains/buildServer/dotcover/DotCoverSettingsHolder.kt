package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants

class DotCoverSettingsHolder(
    private val _parametersService: ParametersService,
    private val _buildInfo: BuildInfo
) {

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled
        get(): Boolean = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: false

    val doNotMergeSnapshots
        get(): Boolean = _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_DO_NOT_MERGE)
            ?.toBooleanStrictOrNull()
            ?: false

    val doNotMakeReport
        get(): Boolean = _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_DO_NOT_REPORT)
            ?.toBooleanStrictOrNull()
            ?: false
}