package jetbrains.buildServer.inspect

object DupFinderConstants {
    const val DATA_PROCESSOR_TYPE = "DotNetDupFinder"
    const val RUNNER_TYPE = "dotnet-tools-dupfinder"
    const val RUNNER_DISPLAY_NAME = "Duplicates finder (ReSharper)"
    const val RUNNER_DESCRIPTION = "Finds C# and VB duplicate code."

    const val SETTINGS_NORMALIZE_TYPES = "$RUNNER_TYPE.hashing.normalize_types"
    const val SETTINGS_DISCARD_LITERALS = "$RUNNER_TYPE.hashing.discard_literals"
    const val SETTINGS_DISCARD_LOCAL_VARIABLES_NAME = "$RUNNER_TYPE.hashing.discard_local_variables_name"
    const val SETTINGS_DISCARD_FIELDS_NAME = "$RUNNER_TYPE.hashing.discard_fields_name"
    const val SETTINGS_DISCARD_TYPES = "$RUNNER_TYPE.hashing.discard_types"

    const val SETTINGS_DISCARD_COST = "$RUNNER_TYPE.discard_cost"
    const val SETTINGS_EXCLUDE_FILES = "$RUNNER_TYPE.exclude_files"
    const val SETTINGS_INCLUDE_FILES = "$RUNNER_TYPE.include_files"
    const val SETTINGS_EXCLUDE_BY_OPENING_COMMENT = "$RUNNER_TYPE.exclude_by_opening_comment"
    const val SETTINGS_EXCLUDE_REGION_MESSAGE_SUBSTRINGS = "$RUNNER_TYPE.exclude_region_message_substring"
    const val SETTINGS_CUSTOM_CMD_ARGS = "$RUNNER_TYPE.customCmdArgs"
    const val SETTINGS_DEBUG = "$RUNNER_TYPE.debug"

    const val DEFAULT_INCLUDE_FILES = "**/*.cs"
    const val DEFAULT_DISCARD_COST = "70"
}