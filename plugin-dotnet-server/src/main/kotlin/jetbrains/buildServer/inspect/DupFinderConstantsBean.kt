/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    fun getRunPlatforms() = IspectionToolPlatform.values().filter { it != IspectionToolPlatform.WindowsX86 }

    fun getRunPlatformName(platformId: String) = IspectionToolPlatform.values().firstOrNull { it.id == platformId }?.displayName ?: platformId
}