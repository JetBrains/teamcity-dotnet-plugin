package jetbrains.buildServer.inspect

enum class InspectionTool(
        val runnerType: String,
        val dysplayName: String,
        val toolName: String,
        val reportArtifactName: String,
        val dataProcessorType: String,
        val customArgs: String,
        val debugSettings: String) {

    Inspectcode(
            InspectCodeConstants.RUNNER_TYPE,
            InspectCodeConstants.RUNNER_DISPLAY_NAME,
            "inspectcode",
            "inspections.zip",
            InspectCodeConstants.DATA_PROCESSOR_TYPE,
            InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS,
            InspectCodeConstants.RUNNER_SETTING_DEBUG
    ),

    Dupfinder(
            DupFinderConstants.RUNNER_TYPE,
            DupFinderConstants.RUNNER_DISPLAY_NAME,
            "dupfinder",
            "duplicates-report.zip",
            DupFinderConstants.DATA_PROCESSOR_TYPE,
            DupFinderConstants.SETTINGS_CUSTOM_CMD_ARGS,
            DupFinderConstants.SETTINGS_DEBUG
    )
}