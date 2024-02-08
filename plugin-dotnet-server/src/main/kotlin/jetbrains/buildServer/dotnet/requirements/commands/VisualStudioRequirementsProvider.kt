package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class VisualStudioRequirementsProvider : DotnetCommandRequirementsProvider {
    override val commandType =  DotnetCommandType.VisualStudio

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        var hasRequirements = false
        parameters[DotnetConstants.PARAM_VISUAL_STUDIO_VERSION]?.let {
            Tool.tryParse(it)?.let {
                if (it.type == ToolType.VisualStudio && it != Tool.VisualStudioAny) {
                    yield(Requirement("VS${it.vsVersion}_Path", null, RequirementType.EXISTS))
                    hasRequirements = true
                }
            }
        }

        if (!hasRequirements) {
            yield(Requirement(RequirementQualifier.EXISTS_QUALIFIER + "VS.+_Path", null, RequirementType.EXISTS))
        }

        yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
    }
}

