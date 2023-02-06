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

object InspectCodeConstants {
    const val DATA_PROCESSOR_TYPE = "ReSharperInspectCode"
    const val RUNNER_TYPE = "dotnet-tools-inspectcode"
    const val RUNNER_DISPLAY_NAME = "Inspections (ReSharper)"
    const val RUNNER_DESCRIPTION = "Runner for gathering JetBrains ReSharper inspection results"

    const val RUNNER_SETTING_CLT_PLUGINS = "jetbrains.resharper-clt.plugins"
    const val RUNNER_SETTING_SOLUTION_PATH = "$RUNNER_TYPE.solution"
    const val RUNNER_SETTING_PROJECT_FILTER = "$RUNNER_TYPE.project.filter"
    const val RUNNER_SETTING_CUSTOM_SETTINGS_PROFILE_PATH = RUNNER_TYPE + "CustomSettingsProfile"
    const val RUNNER_SETTING_DEBUG = "$RUNNER_TYPE.debug"
    const val RUNNER_SETTING_CUSTOM_CMD_ARGS = "$RUNNER_TYPE.customCmdArgs"

    const val CONFIG_PARAMETER_SUPRESS_BUILD_IN_SETTINGS = "teamcity.dotNetTools.inspecitons.supressBuildInSettings"
    const val CONFIG_PARAMETER_DISABLE_SOLUTION_WIDE_ANALYSIS = "teamcity.dotNetTools.inspecitons.disableSolutionWideAnalysis"
}