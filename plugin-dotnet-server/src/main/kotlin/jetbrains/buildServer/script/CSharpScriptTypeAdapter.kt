/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import jetbrains.buildServer.script.ScriptConstants.CLT_TOOL_TYPE_ID
import jetbrains.buildServer.tools.ToolTypeAdapter

class CSharpScriptTypeAdapter : ToolTypeAdapter() {
    override fun getType()= ScriptConstants.CLT_TOOL_TYPE_ID

    override fun getDisplayName() = ScriptConstants.CLT_TOOL_TYPE_NAME

    override fun getDescription(): String = "Is used in C# script build steps."

    override fun getShortDisplayName() = "C# script tool"

    override fun getTargetFileDisplayName() = "TeamCity C# script tool"

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://www.nuget.org/packages/$CLT_TOOL_TYPE_ID/"

    override fun getToolLicenseUrl() = "https://raw.githubusercontent.com/NikolayPianikov/teamcity-csharp-interactive/master/LICENSE"

    override fun getValidPackageDescription() =
            "Specify the path to a C# Command Line Tools package (.zip or .nupkg).\n" +
            "<br/>Download <em>dotnet-csi.&lt;VERSION&gt;.nupkg</em> from\n" +
            "<a href=\"https://www.nuget.org/packages/dotnet-csi\" target=\"_blank\" rel=\"noreferrer\">www.nuget.org</a>"
}