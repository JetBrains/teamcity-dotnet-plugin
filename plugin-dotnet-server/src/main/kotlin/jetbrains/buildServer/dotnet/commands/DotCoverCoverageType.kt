package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

class DotCoverCoverageType : CommandType() {
    override val name: String
        get() = CoverageConstants.PARAM_DOTCOVER

    override val description: String
        get() = "JetBrains dotCover"

    override val editPage: String
        get() = "editDotCoverParameters.jsp"

    override val viewPage: String
        get() = "viewDotCoverParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = buildSequence {
        yieldAll(super.validateProperties(properties))

        if (properties[CoverageConstants.PARAM_DOTCOVER_HOME].isNullOrBlank()) {
            yield(InvalidProperty(CoverageConstants.PARAM_DOTCOVER_HOME, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>) = buildSequence {
        yieldAll(super.getRequirements(parameters))
        yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
    }
}