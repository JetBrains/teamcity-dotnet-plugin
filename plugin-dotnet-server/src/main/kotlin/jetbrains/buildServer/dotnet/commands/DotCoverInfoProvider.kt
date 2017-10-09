package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotCoverConstants

class DotCoverInfoProvider {
    fun isCoverageEnabled(parameters: Map<String, String>): Boolean =
            parameters[DotCoverConstants.PARAM_ENABLED]?.equals("true", true) ?: false
}