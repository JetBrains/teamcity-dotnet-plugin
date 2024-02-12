package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_HOME
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory
import jetbrains.buildServer.serverSide.InvalidProperty

class DotCoverCoverageType(
    sdkBasedRequirementFactory: SdkBasedRequirementFactory
) : CommandType(sdkBasedRequirementFactory) {
    override val name: String = PARAM_DOTCOVER

    override val description: String = "JetBrains dotCover"

    override val editPage: String = "editDotCoverParameters.jsp"

    override val viewPage: String = "viewDotCoverParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[PARAM_DOTCOVER_HOME].isNullOrBlank()) {
            yield(InvalidProperty(PARAM_DOTCOVER_HOME, DotnetConstants.VALIDATION_EMPTY))
        }
    }
}