package jetbrains.buildServer.dotnet.requirements

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.requirements.Requirement

class RequiredDotnetSDKRequirementsProvider(
    private val _sdkBasedRequirementFactory: SdkBasedRequirementFactory,
) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Sequence<Requirement> =
        parameters[DotnetConstants.PARAM_REQUIRED_SDK]
            ?.split(" ", "\n", ";")
            ?.map { it.trim() }
            ?.filter { !it.isNullOrBlank() }
            ?.map { _sdkBasedRequirementFactory.tryCreate(it) }
            ?.mapNotNull { it }
            ?.asSequence()
            ?: emptySequence()
}