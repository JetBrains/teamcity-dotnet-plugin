package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.inspect.CltConstants.CLT_PATH_PARAMETER
import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.tools.ServerToolManager

class RequirementsProviderImpl(
        private val _toolVersionProvider: ToolVersionProvider,
        private val _requirementsResolver: RequirementsResolver)
    : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Collection<Requirement> =
            _requirementsResolver.resolve(
                    _toolVersionProvider.getVersion(parameters),
                    getPlatform(parameters)
            ).toList()

    private fun getPlatform(parameters: Map<String, String>): IspectionToolPlatform =
            parameters[RUNNER_SETTING_CLT_PLATFORM]
                    ?.let { IspectionToolPlatform.tryParse(it) }
                    ?: IspectionToolPlatform.X64
}