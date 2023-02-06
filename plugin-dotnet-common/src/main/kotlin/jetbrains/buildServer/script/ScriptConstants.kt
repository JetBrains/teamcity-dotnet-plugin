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

package jetbrains.buildServer.script

object ScriptConstants {
    const val RUNNER_TYPE = "csharpScript"
    const val RUNNER_DISPLAY_NAME = "C# Script"
    const val RUNNER_DESCRIPTION = "C# Script runner"

    const val SCRIPT_TYPE = "scriptType"
    const val SCRIPT_CONTENT = "scriptContent"
    const val SCRIPT_FILE = "scriptFile"
    const val ARGS = "scriptArgs"
    const val CLT_PATH = "csharpToolPath"
    const val NUGET_PACKAGE_SOURCES = "nuget.packageSources"
    const val TOOL_PATH = "scriptToolPath"

    const val CLT_TOOL_TYPE_ID = "TeamCity.csi"
    const val CLT_TOOL_TYPE_NAME = "C# script"

    const val RUNNER_ENABLED = "teamcity.internal.csharp.script"
}