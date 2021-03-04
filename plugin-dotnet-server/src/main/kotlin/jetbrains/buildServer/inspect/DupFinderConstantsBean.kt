package jetbrains.buildServer.inspect

class DupFinderConstantsBean {
    fun DupFinderConstantsBean() {}

    fun getDiscardLiteralsKey(): String {
        return DupFinderConstants.SETTINGS_DISCARD_LITERALS
    }

    fun getDiscardLocalVariablesNameKey(): String {
        return DupFinderConstants.SETTINGS_DISCARD_LOCAL_VARIABLES_NAME
    }

    fun getDiscardFieldsNameKey(): String {
        return DupFinderConstants.SETTINGS_DISCARD_FIELDS_NAME
    }

    fun getDiscardTypesKey(): String {
        return DupFinderConstants.SETTINGS_DISCARD_TYPES
    }

    fun getDiscardCostKey(): String {
        return DupFinderConstants.SETTINGS_DISCARD_COST
    }

    fun getExcludeFilesKey(): String {
        return DupFinderConstants.SETTINGS_EXCLUDE_FILES
    }

    fun getIncludeFilesKey(): String {
        return DupFinderConstants.SETTINGS_INCLUDE_FILES
    }

    fun getExcludeByOpeningCommentKey(): String {
        return DupFinderConstants.SETTINGS_EXCLUDE_BY_OPENING_COMMENT
    }

    fun getExcludeRegionMessageSubstringsKey(): String {
        return DupFinderConstants.SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS
    }

    fun getDebugKey(): String {
        return DupFinderConstants.SETTINGS_DEBUG
    }

    fun getCustomCommandlineKey(): String {
        return DupFinderConstants.SETTINGS_CUSTOM_CMD_ARGS
    }

    fun getNormalizeTypesKey(): String {
        return DupFinderConstants.SETTINGS_NORMALIZE_TYPES
    }

    fun getCltPathKey(): String {
        return CltConstants.CLT_PATH_PARAMETER
    }

    fun getCltToolTypeName(): String {
        return CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
    }
}