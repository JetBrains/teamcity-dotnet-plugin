package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.TargetDotNetFramework

class InspectCodeConstantsBean {
    fun InspectCodeConstantsBean() {}

    fun getSolutionPathKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
    }

    fun getCustomSettingsProfilePathKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH
    }

    fun getProjectFilerKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER
    }

    fun getDebugKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_DEBUG
    }

    fun getCustomCommandlineKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS
    }

    fun getAvailableTargetFrameworks(): Array<TargetDotNetFramework> {
        return TargetDotNetFramework.values()
    }

    fun getCltPathKey(): String {
        return CltConstants.CLT_PATH_PARAMETER
    }

    fun getCltToolTypeName(): String {
        return CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
    }

    fun getCltPlatformKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM
    }

    fun getCltPluginsKey(): String {
        return InspectCodeConstants.RUNNER_SETTING_CLT_PLUGINS
    }

    fun getRunPlatforms(): Collection<String> {
        return listOf(
                InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM_X64_PARAMETER,
                InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER
        )
    }
}