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
import jetbrains.buildServer.requirements.Requirement

class RequirementsProviderImpl(
        private val _toolVersionProvider: ToolVersionProvider,
        private val _requirementsResolver: RequirementsResolver)
    : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>): Collection<Requirement> =
            _requirementsResolver.resolve(
                    _toolVersionProvider.getVersion(parameters[CltConstants.CLT_PATH_PARAMETER], CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID),
                    getPlatform(parameters)
            ).toList()

    private fun getPlatform(parameters: Map<String, String>): InspectionToolPlatform =
            parameters[RUNNER_SETTING_CLT_PLATFORM]
                    ?.let { InspectionToolPlatform.tryParse(it) }
                    ?: InspectionToolPlatform.WindowsX64
}