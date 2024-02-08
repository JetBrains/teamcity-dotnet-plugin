package jetbrains.buildServer.dotnet.requirements

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.requirements.commands.DotnetCommandRequirementsProvider

class DotnetRunnerRequirementsProvider(
    private val _commandRequirementsProvider: List<DotnetCommandRequirementsProvider>,
    private val _sdkRequirementsProvider: RequirementsProvider,
    private val _dotCoverRequirementsProvider: RequirementsProvider,
) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>) = sequence {
        // no requirements in a container
        if (!parameters[DotnetConstants.PARAM_DOCKER_IMAGE].isNullOrEmpty()) {
            return@sequence
        }

        // .NET tools (.NET CLI, MSBuild, VSTest, Visual Studio, etc.) related commands requirements
        parameters[DotnetConstants.PARAM_COMMAND]
            ?.let { commandTypeName -> _commandRequirementsProvider.filter { it.commandType.id == commandTypeName } }
            ?.plus(_sdkRequirementsProvider)
            ?.flatMap { it.getRequirements(parameters) }
            ?.let { yieldAll(it) }

        // coverage types related commands requirements
        parameters[CoverageConstants.PARAM_TYPE]
            ?.let { when (it) {
                CoverageConstants.PARAM_DOTCOVER -> _dotCoverRequirementsProvider.getRequirements(parameters)
                else -> emptySequence()
            }}
            ?.let { yieldAll(it) }
    }
}