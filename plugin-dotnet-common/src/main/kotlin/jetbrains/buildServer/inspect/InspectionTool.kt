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

enum class InspectionTool(
    val runnerType: String,
    val displayName: String,
    val toolName: String,
    val reportArtifactName: String,
    val dataProcessorType: String,
    val customArgs: String,
    val debugSettings: String,
) {
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