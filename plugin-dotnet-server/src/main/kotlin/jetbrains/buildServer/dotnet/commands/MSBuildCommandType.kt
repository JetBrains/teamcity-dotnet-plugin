package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet MSBuild command.
 */
class MSBuildCommandType(
        private val _msbuildRequirementsProvider: MSBuildRequirementsProvider,
        private val _dotCoverInfoProvider: DotCoverInfoProvider) : CommandType() {
    override val name: String
        get() = DotnetCommandType.MSBuild.id

    override val editPage: String
        get() = "editMSBuildParameters.jsp"

    override val viewPage: String
        get() = "viewMSBuildParameters.jsp"

    override fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> {
        val invalidProperties = arrayListOf<InvalidProperty>()
        if (_dotCoverInfoProvider.isCoverageEnabled(properties)) {
            if (properties[DotCoverConstants.PARAM_HOME].isNullOrBlank()) {
                invalidProperties.add(InvalidProperty(DotCoverConstants.PARAM_HOME, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        return invalidProperties
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        _msbuildRequirementsProvider.getRequirements(runParameters)
    }
}