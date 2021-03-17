package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.TargetDotNetFramework

class InspectCodeConstantsBean {
    fun getSolutionPathKey() = InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH

    fun getCustomSettingsProfilePathKey() = InspectCodeConstants.RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH

    fun getProjectFilerKey() = InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER

    fun getDebugKey() = InspectCodeConstants.RUNNER_SETTING_DEBUG

    fun getCustomCommandlineKey() = InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS

    fun getAvailableTargetFrameworks(): Array<TargetDotNetFramework> = TargetDotNetFramework.values()

    fun getCltPathKey() = CltConstants.CLT_PATH_PARAMETER

    fun getCltToolTypeName() = CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID

    fun getCltPlatformKey() = CltConstants.RUNNER_SETTING_CLT_PLATFORM

    fun getCltPluginsKey() = InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS

    fun getRunPlatforms(): Collection<String> = IspectionToolPlatform.values().map { it.id }
}