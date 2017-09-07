package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.CommandType
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet build id.
 */
class MSBuildCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.MSBuild.id

    override val editPage: String
        get() = "editMSBuildParameters.jsp"

    override val viewPage: String
        get() = "viewMSBuildParameters.jsp"

    override fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> {
        val invalidProperties = arrayListOf<InvalidProperty>()
        if (isCoverageEnabled(properties)) {
            if (properties[DotCoverConstants.PARAM_HOME].isNullOrBlank()) {
                invalidProperties.add(InvalidProperty(DotCoverConstants.PARAM_HOME, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        return invalidProperties
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        if(isCoverageEnabled(runParameters)) {
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }

    private fun isCoverageEnabled(parameters: Map<String, String>): Boolean = parameters[DotCoverConstants.PARAM_ENABLED]?.equals("true", true) ?: false
}