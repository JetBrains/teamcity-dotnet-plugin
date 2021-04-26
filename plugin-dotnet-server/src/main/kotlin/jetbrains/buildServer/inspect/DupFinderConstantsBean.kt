package jetbrains.buildServer.inspect

class DupFinderConstantsBean {
    fun getDiscardLiteralsKey() = DupFinderConstants.SETTINGS_DISCARD_LITERALS

    fun getDiscardLocalVariablesNameKey() = DupFinderConstants.SETTINGS_DISCARD_LOCAL_VARIABLES_NAME

    fun getDiscardFieldsNameKey() = DupFinderConstants.SETTINGS_DISCARD_FIELDS_NAME

    fun getDiscardTypesKey() = DupFinderConstants.SETTINGS_DISCARD_TYPES

    fun getDiscardCostKey() = DupFinderConstants.SETTINGS_DISCARD_COST

    fun getExcludeFilesKey() = DupFinderConstants.SETTINGS_EXCLUDE_FILES

    fun getIncludeFilesKey() = DupFinderConstants.SETTINGS_INCLUDE_FILES

    fun getExcludeByOpeningCommentKey() = DupFinderConstants.SETTINGS_EXCLUDE_BY_OPENING_COMMENT

    fun getExcludeRegionMessageSubstringsKey() = DupFinderConstants.SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS

    fun getDebugKey() = DupFinderConstants.SETTINGS_DEBUG

    fun getCustomCommandlineKey() = DupFinderConstants.SETTINGS_CUSTOM_CMD_ARGS

    fun getNormalizeTypesKey() = DupFinderConstants.SETTINGS_NORMALIZE_TYPES

    fun getCltPlatformKey() = CltConstants.RUNNER_SETTING_CLT_PLATFORM

    fun getCltPathKey() = CltConstants.CLT_PATH_PARAMETER

    fun getCltToolTypeName() = CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID

    fun getRunPlatforms(): Collection<String> = IspectionToolPlatform.values().filter { it != IspectionToolPlatform.X86 }.map { it.id }
}