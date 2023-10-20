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

import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_SOLUTION_PATH
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class InspectCodeRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val solutionPath = properties[RUNNER_SETTING_SOLUTION_PATH]
        if (PropertiesUtil.isEmptyOrNull(solutionPath)) {
            result.add(InvalidProperty(RUNNER_SETTING_SOLUTION_PATH,"Solution path must be specified"))
        }

        val platform = properties[RUNNER_SETTING_CLT_PLATFORM]?.let {
            InspectionToolPlatform.tryParse(it)
        }

        if(platform == InspectionToolPlatform.CrossPlatform && _toolVersionProvider.getVersion(properties[CltConstants.CLT_PATH_PARAMETER], CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID) < RequirementsResolverImpl.CrossPlatformVersion) {
            result.add(InvalidProperty(RUNNER_SETTING_CLT_PLATFORM,"To support cross-platform inspections, use ReSharper version ${RequirementsResolverImpl.CrossPlatformVersion} or later."))
        }

        return result
    }
}