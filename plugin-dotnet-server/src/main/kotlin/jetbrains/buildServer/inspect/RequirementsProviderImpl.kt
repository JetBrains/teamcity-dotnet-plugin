package jetbrains.buildServer.inspect

import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.requirements.Requirement

class RequirementsProviderImpl(
        private val _toolVersionProvider: ToolVersionProvider,
        private val _requirementsResolver: RequirementsResolver)
    : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Collection<Requirement> =
            _requirementsResolver.resolve(
                    _toolVersionProvider.getVersion(parameters[CltConstants.CLT_PATH_PARAMETER], CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID),
                    getPlatform(parameters)
            ).toList()

    private fun getPlatform(parameters: Map<String, String>): IspectionToolPlatform =
            parameters[RUNNER_SETTING_CLT_PLATFORM]
                    ?.let { IspectionToolPlatform.tryParse(it) }
                    ?: IspectionToolPlatform.WindowsX64
}