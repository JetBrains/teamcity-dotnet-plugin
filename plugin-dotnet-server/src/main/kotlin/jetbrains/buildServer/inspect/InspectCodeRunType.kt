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

import jetbrains.buildServer.dotnet.TargetDotNetFramework
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.RunType
import jetbrains.buildServer.serverSide.RunTypeRegistry
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor

class InspectCodeRunType(
        runTypeRegistry: RunTypeRegistry,
        private val _pluginDescriptor: PluginDescriptor,
        private val _requirementsProvider: RequirementsProvider,
        private val _propertiesProcessor: PropertiesProcessor)
    : RunType() {

    init {
        runTypeRegistry.registerRunType(this)
    }

    override fun getRunnerPropertiesProcessor() =
        _propertiesProcessor

    override fun getDescription() = InspectCodeConstants.RUNNER_DESCRIPTION

    override fun getEditRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("editInspectCodeRunParams.jsp")

    override fun getViewRunnerParamsJspFilePath() =
        _pluginDescriptor.getPluginResourcesPath("viewInspectCodeRunParams.jsp")

    override fun getDefaultRunnerProperties(): Map<String, String> = HashMap()

    override fun getType() = InspectCodeConstants.RUNNER_TYPE

    override fun getDisplayName() = InspectCodeConstants.RUNNER_DISPLAY_NAME

    override fun describeParameters(parameters: Map<String, String>): String {
        val solutionPath = parameters[InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH]
        val projectFilter = parameters[InspectCodeConstants.RUNNER_SETTING_PROJECT_FILTER]
        val sb = StringBuilder()
        if (!StringUtil.isEmptyOrSpaces(solutionPath)) {
            sb.append("Solution file path: ").append(solutionPath).append("\n")
        }

        sb.append("Sources to analyze: ")
        if (StringUtil.isEmptyOrSpaces(projectFilter)) {
            sb.append("whole solution").append("\n")
        } else {
            sb.append(projectFilter).append("\n")
        }

        return sb.toString()
    }

    override fun getRunnerSpecificRequirements(runParameters: Map<String, String>): List<Requirement> {
        val requirements = _requirementsProvider.getRequirements(runParameters).toMutableList()
        for (targetFramework in TargetDotNetFramework.values()) {
            if (!runParameters.containsKey(targetFramework.id)) continue
            requirements.add(targetFramework.createExistsRequirement())
        }

        return requirements
    }

    override fun getTags(): MutableSet<String> {
        return mutableSetOf(".NET", "code analysis", "ReSharper")
    }

    override fun getIconUrl(): String {
        return _pluginDescriptor.getPluginResourcesPath("resharper.svg")
    }
}
