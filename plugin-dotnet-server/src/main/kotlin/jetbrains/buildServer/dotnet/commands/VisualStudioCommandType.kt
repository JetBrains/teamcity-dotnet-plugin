package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.requirements.SDKBasedRequirementFactory
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for devenv.com /build command.
 */
class VisualStudioCommandType(
    sdkBasedRequirementFactory: SDKBasedRequirementFactory
) : CommandType(sdkBasedRequirementFactory) {
    override val name: String = DotnetCommandType.VisualStudio.id

    override val editPage: String = "editVisualStudioParameters.jsp"

    override val viewPage: String = "viewVisualStudioParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[DotnetConstants.PARAM_PATHS].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_PATHS, DotnetConstants.VALIDATION_EMPTY))
        }

        if (properties[DotnetConstants.PARAM_VISUAL_STUDIO_ACTION].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION, DotnetConstants.VALIDATION_EMPTY))
        }
    }
}