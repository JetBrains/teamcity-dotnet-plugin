package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants

object DotCoverRunnerProperties {
    fun getDefaultRunnerProperties() = mapOf(
        CoverageConstants.PARAM_DOTCOVER_HOME
                to "%teamcity.tool.${CoverageConstants.DOTCOVER_PACKAGE_ID}.DEFAULT%"
    )
}