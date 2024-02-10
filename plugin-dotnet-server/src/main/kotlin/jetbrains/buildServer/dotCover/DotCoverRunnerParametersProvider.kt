package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants

/**
 * Provides parameters for dotCover runner
 */
class DotCoverRunnerParametersProvider {
    val dotCoverHomeKey = CoverageConstants.PARAM_DOTCOVER_HOME
    val dotCoverCommandLineKey = CoverageConstants.PARAM_DOTCOVER_COMMAND_LINE
    val dotCoverFiltersKey = CoverageConstants.PARAM_DOTCOVER_FILTERS
    val dotCoverAttributeFiltersKey = CoverageConstants.PARAM_DOTCOVER_ATTRIBUTE_FILTERS
    val dotCoverArgumentsKey = CoverageConstants.PARAM_DOTCOVER_ARGUMENTS
    val dotCoverGenerateReportKey = CoverageConstants.PARAM_DOTCOVER_GENERATE_REPORT
    val dotCoverAdditionalShapshotPathsKey = CoverageConstants.PARAM_DOTCOVER_ADDITIONAL_SNAPSHOT_PATHS
}
