package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.serverSide.ParametersPreprocessor
import jetbrains.buildServer.serverSide.SRunningBuild

class DotCoverParametersPreprocessor : ParametersPreprocessor {

    override fun fixRunBuildParameters(
        build: SRunningBuild,
        runParameters: MutableMap<String, String>,
        buildParams: Map<String, String>
    ) {
        if (dotCoverDisabled(runParameters) && runParameters[CoverageConstants.PARAM_DOTCOVER_HOME] != null) {
            //it's needed to remove reference on tool from runner parameters.
            //otherwise if dotcover tool doesn't exist on agent
            //agent will report warning message in build log. See TW-60495
            runParameters.remove(CoverageConstants.PARAM_DOTCOVER_HOME)
        }
    }

    private fun dotCoverDisabled(runParameters: Map<String, String>): Boolean {
        val coverageType = runParameters[CoverageConstants.PARAM_TYPE] ?: ""
        return if (coverageType.isEmpty()) true else CoverageConstants.PARAM_DOTCOVER != coverageType
    }
}
