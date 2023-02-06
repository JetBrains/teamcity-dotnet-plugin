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

import jetbrains.buildServer.inspect.DupFinderConstants.DEFAULT_DISCARD_COST
import jetbrains.buildServer.inspect.DupFinderConstants.DEFAULT_INCLUDE_FILES
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor

class DupFinderRunType(
        runTypeRegistry: RunTypeRegistry,
        private val _pluginDescriptor: PluginDescriptor,
        private val _requirementsProvider: RequirementsProvider,
        private val _propertiesProcessor: PropertiesProcessor)
    : RunType() {
    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getRunnerPropertiesProcessor() = _propertiesProcessor

    override fun getDescription() = DupFinderConstants.RUNNER_DESCRIPTION

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editDupFinderRunParams.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewDupFinderRunParams.jsp")

    override fun getDefaultRunnerProperties() = mapOf(
            DupFinderConstants.SETTINGS_INCLUDE_FILES to DEFAULT_INCLUDE_FILES,
            DupFinderConstants.SETTINGS_DISCARD_COST to DEFAULT_DISCARD_COST,
            DupFinderConstants.SETTINGS_DISCARD_LITERALS to true.toString(),
            CltConstants.RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.WindowsX64.id)

    override fun getType() = DupFinderConstants.RUNNER_TYPE

    override fun getDisplayName() = DupFinderConstants.RUNNER_DISPLAY_NAME

    override fun getTags(): MutableSet<String> {
        return mutableSetOf(".NET", "ReSharper", "code analysis")
    }

    override fun describeParameters(parameters: Map<String, String>): String {
        val includes = parameters[DupFinderConstants.SETTINGS_INCLUDE_FILES]
        val excludes = parameters[DupFinderConstants.SETTINGS_EXCLUDE_FILES]
        val sb = StringBuilder()
        if (!StringUtil.isEmptyOrSpaces(includes)) {
            sb.append("Include sources: ").append(includes).append("\n")
        }

        if (!StringUtil.isEmptyOrSpaces(excludes)) {
            sb.append("Exclude sources: : ").append(excludes).append("\n")
        }

        return sb.toString()
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>) =
        _requirementsProvider.getRequirements(runParameters).toList()

    override fun getIconUrl(): String {
        return _pluginDescriptor.getPluginResourcesPath("resharper.svg")
    }
}
