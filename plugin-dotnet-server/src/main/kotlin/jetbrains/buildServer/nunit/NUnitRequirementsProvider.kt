package jetbrains.buildServer.nunit

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementQualifier
import jetbrains.buildServer.requirements.RequirementType

class NUnitRequirementsProvider(
    private val _dotCoverRequirementsProvider: RequirementsProvider,
) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>) = sequence {
        // no requirements in a container
        if (!parameters[DotnetConstants.PARAM_DOCKER_IMAGE].isNullOrEmpty()) {
            return@sequence
        }

        // mono or any .NET
        val dotnetFrameworkRegex = DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK + ".*"
        val monoRegex = MonoConstants.CONFIG_PATH
        yield(
            Requirement(
                RequirementQualifier.EXISTS_QUALIFIER + "(" + dotnetFrameworkRegex + "|" + monoRegex + ")",
                null,
                RequirementType.EXISTS
            )
        )

        // coverage types related commands requirements
        parameters[CoverageConstants.PARAM_TYPE]
            ?.let {
                when (it) {
                    CoverageConstants.PARAM_DOTCOVER -> _dotCoverRequirementsProvider.getRequirements(parameters)
                    else -> emptySequence()
                }
            }
            ?.let { yieldAll(it) }
    }
}