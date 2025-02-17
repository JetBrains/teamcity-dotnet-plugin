package jetbrains.buildServer.inspect

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.TeamCityProperties

class ReSharperRequirementsProvider(
    private val _toolVersionProvider: ToolVersionProvider,
    private val _requirementsResolver: RequirementsResolver,
) : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Sequence<Requirement> {
        if (!TeamCityProperties.getBooleanOrTrue(CltConstants.CLT_AGENT_REQUIREMENTS_ENABLED)) return emptySequence()

        return _toolVersionProvider.getVersion(parameters[CltConstants.CLT_PATH_PARAMETER], CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID)
            .let { it -> _requirementsResolver.resolve(it, getPlatform(parameters)) }
            .toList().asSequence()
    }

    private fun getPlatform(parameters: Map<String, String>): InspectionToolPlatform =
        parameters[CltConstants.RUNNER_SETTING_CLT_PLATFORM]
            ?.let { InspectionToolPlatform.tryParse(it) }
            ?: InspectionToolPlatform.WindowsX64
}