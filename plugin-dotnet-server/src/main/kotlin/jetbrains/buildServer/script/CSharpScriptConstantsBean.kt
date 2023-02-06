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

class CSharpScriptConstantsBean {
    val scriptType = ScriptConstants.SCRIPT_TYPE
    val scriptContent = ScriptConstants.SCRIPT_CONTENT
    val scriptFile = ScriptConstants.SCRIPT_FILE
    val cltPath = ScriptConstants.CLT_PATH
    val nugetPackageSources = ScriptConstants.NUGET_PACKAGE_SOURCES
    val args = ScriptConstants.ARGS
    val toolPath = ScriptConstants.TOOL_PATH

    val typeFile = ScriptType.File.id
    val typeFileDescription = ScriptType.File.description
    val typeCustom = ScriptType.Custom.id
    val typeCustomDescription = ScriptType.Custom.description

    val cltToolTypeName = ScriptConstants.CLT_TOOL_TYPE_ID
}