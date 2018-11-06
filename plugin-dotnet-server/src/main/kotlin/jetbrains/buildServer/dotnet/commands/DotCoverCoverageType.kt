@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty

class DotCoverCoverageType : CommandType() {
    override val name: String = CoverageConstants.PARAM_DOTCOVER

    override val description: String = "JetBrains dotCover"

    override val editPage: String = "editDotCoverParameters.jsp"

    override val viewPage: String = "viewDotCoverParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[CoverageConstants.PARAM_DOTCOVER_HOME].isNullOrBlank()) {
            yield(InvalidProperty(CoverageConstants.PARAM_DOTCOVER_HOME, DotnetConstants.VALIDATION_EMPTY))
        }
    }

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        yieldAll(super.getRequirements(parameters))

        if (isDocker(parameters)) return@sequence

        yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "(DotNetFramework3\\.5_.+|DotNetFramework4\\.[\\d\\.]+_.+)", null, RequirementType.EXISTS))
    }
}