package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.BuildInfo
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.EventDispatcher

class DotCoverSettingsHolder(
    private val _parametersService: ParametersService,
    private val _buildInfo: BuildInfo
) {

    val buildStepId get() = _buildInfo.id

    val coveragePostProcessingEnabled
        get(): Boolean = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: true

    // TODO: detect based on the runner parameters
    val applyMergeCommand get(): Boolean = true

    // TODO: detect based on the runner parameters
    val applyReportCommand get(): Boolean = true
}