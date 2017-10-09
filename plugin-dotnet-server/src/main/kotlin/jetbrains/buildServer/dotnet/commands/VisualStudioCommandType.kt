package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for devenv.com /build command.
 */
class VisualStudioCommandType(
        private val _visualStudioRequirementsProvider: VisualStudioRequirementsProvider,
        private val _dotCoverInfoProvider: DotCoverInfoProvider) : CommandType() {
    override val name: String
        get() = DotnetCommandType.VisualStudio.id

    override val editPage: String
        get() = "editVisualStudioParameters.jsp"

    override val viewPage: String
        get() = "viewVisualStudioParameters.jsp"

    override fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> {
        val invalidProperties = arrayListOf<InvalidProperty>()

        if (properties[DotnetConstants.PARAM_VISUAL_STUDIO_ACTION].isNullOrBlank()) {
            invalidProperties.add(InvalidProperty(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION, DotnetConstants.VALIDATION_EMPTY))
        }

        if (_dotCoverInfoProvider.isCoverageEnabled(properties)) {
            if (properties[DotCoverConstants.PARAM_HOME].isNullOrBlank()) {
                invalidProperties.add(InvalidProperty(DotCoverConstants.PARAM_HOME, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        return invalidProperties
    }

    override fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = _visualStudioRequirementsProvider.getRequirements(runParameters)
}