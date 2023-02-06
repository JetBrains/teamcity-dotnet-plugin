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
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.util.PropertiesUtil
import java.util.*

class DupFinderRunTypePropertiesProcessor(
        private val _toolVersionProvider: ToolVersionProvider)
    : PropertiesProcessor {
    override fun process(properties: Map<String, String>): Collection<InvalidProperty> {
        val result: MutableList<InvalidProperty> = Vector()
        val files = properties[DupFinderConstants.SETTINGS_INCLUDE_FILES]
        if (PropertiesUtil.isEmptyOrNull(files)) {
            result.add(InvalidProperty(DupFinderConstants.SETTINGS_INCLUDE_FILES, "Input files must be specified"))
        }

        val discardCostValue = properties[DupFinderConstants.SETTINGS_DISCARD_COST]
        if (!PropertiesUtil.isEmptyOrNull(discardCostValue)) {
            if (!ReferencesResolverUtil.isReference(discardCostValue!!)) {
                val value = PropertiesUtil.parseInt(discardCostValue)
                if (value == null || value <= 0) {
                    result.add(InvalidProperty(DupFinderConstants.SETTINGS_DISCARD_COST, "Duplicate complexity must be a positive number or parameter reference. "))
                }
            }
        }

        val platform = properties[CltConstants.RUNNER_SETTING_CLT_PLATFORM]?.let {
            IspectionToolPlatform.tryParse(it)
        }

        if(platform == IspectionToolPlatform.CrossPlatform && _toolVersionProvider.getVersion(properties[CltConstants.CLT_PATH_PARAMETER], CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID) < RequirementsResolverImpl.CrossPlatformVersion) {
            result.add(InvalidProperty(CltConstants.RUNNER_SETTING_CLT_PLATFORM,"To support cross-platform duplicates finder, use ReSharper version ${RequirementsResolverImpl.CrossPlatformVersion} or later."))
        }

        return result
    }
}