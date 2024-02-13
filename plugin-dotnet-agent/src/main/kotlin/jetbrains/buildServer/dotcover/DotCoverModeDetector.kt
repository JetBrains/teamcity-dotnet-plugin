package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants

class DotCoverModeDetector {
    fun detectMode(runnerType: String, runParameters: Map<String, String>) = when(runnerType) {
        // dotCover runner
        CoverageConstants.DOTCOVER_RUNNER_TYPE -> DotCoverMode.Runner

        // .NET runner
        DotnetConstants.RUNNER_TYPE ->  when {
            isDotCoverWrapperEnabled(runParameters) -> DotCoverMode.Wrapper
            else -> DotCoverMode.Disabled
        }

        else -> DotCoverMode.Disabled
    }

    private fun isDotCoverWrapperEnabled(runParameters: Map<String, String>) = when {
        runParameters.validateParameter(CoverageConstants.PARAM_TYPE) {
            it == CoverageConstants.PARAM_DOTCOVER
        } -> true

        runParameters.validateParameter("dotNetCoverage.dotCover.enabled") {
            it.trim().toBoolean()
        } -> true

        else -> false
    }

    private fun Map<String, String>.validateParameter(parameter: String, validator: (String) -> Boolean) =
        this[parameter]?.let { validator(it) } ?: false
}