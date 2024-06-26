package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants

/**
 * Provides parameters for dotCover runner
 */
class DotCoverRunnerParametersProvider {
    val coverageToolTypeKey = CoverageConstants.PARAM_TYPE
    val coverageToolTypeValue = CoverageConstants.PARAM_DOTCOVER
    val dotCoverHomeKey = CoverageConstants.PARAM_DOTCOVER_HOME
    val dotCoverCoveredProcessExecutableKey = CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_EXECUTABLE
    val dotCoverCoveredProcessArgumentsKey = CoverageConstants.PARAM_DOTCOVER_COVERED_PROCESS_ARGUMENTS
    val dotCoverFiltersKey = CoverageConstants.PARAM_DOTCOVER_FILTERS
    val dotCoverAttributeFiltersKey = CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS
    val dotCoverArgumentsKey = CoverageConstants.PARAM_DOTCOVER_ARGUMENTS
    val dotCoverGenerateReportKey = CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT
    val dotCoverAdditionalShapshotPathsKey = CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS
}
