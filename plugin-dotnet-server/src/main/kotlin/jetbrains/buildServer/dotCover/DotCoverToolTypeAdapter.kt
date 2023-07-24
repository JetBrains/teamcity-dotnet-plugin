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

package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.tools.ToolTypeAdapter

class DotCoverToolTypeAdapter : ToolTypeAdapter() {

    override fun getType() = CoverageConstants.DOTCOVER_PACKAGE_ID

    override fun getDisplayName() = CoverageConstants.DOT_COVER_TOOL_TYPE_NAME

    override fun getDescription(): String = "Is used in JetBrains dotCover-specific build steps to get code coverage."

    override fun getShortDisplayName() = CoverageConstants.DOT_COVER_SHORT_TOOL_TYPE_NAME

    override fun getTargetFileDisplayName() = CoverageConstants.DOT_COVER_TARGET_FILE_DISPLAY_NAME

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://www.jetbrains.com/dotcover/download/#section=commandline"

    override fun getToolLicenseUrl() = "https://www.jetbrains.com/dotcover/download/command_line_license.html"

    override fun getTeamCityHelpFile() = "JetBrains+dotCover"

    override fun getValidPackageDescription() = "Specify the path to a " + displayName +  " (.zip or .nupkg).\n" +
            "<br/><br/>Supported tools for Windows:" +
            "<br/><a href=\"https://www.jetbrains.com/dotcover/download/#section=commandline\" target=\"_blank\" rel=\"noreferrer\">JetBrains.dotCover.CommandLineTools.&lt;VERSION&gt;.zip</a>" +
            "<br/><a href=\"https://www.nuget.org/packages/JetBrains.dotCover.CommandLineTools/\" target=\"_blank\" rel=\"noreferrer\">JetBrains.dotCover.CommandLineTools.&lt;VERSION&gt;.nupkg</a>" +
            "<br/><br/>Supported cross-platform tool:" +
            "<br/><a href=\"https://www.nuget.org/packages/JetBrains.dotCover.DotNetCliTool/\" target=\"_blank\" rel=\"noreferrer\">JetBrains.dotCover.DotNetCliTool.&lt;VERSION&gt;.nupkg</a>" +
            "<br/><br/>Supported tools for MacOS arm64:" +
            "<br/><a href=\"https://www.nuget.org/packages/JetBrains.dotCover.CommandLineTools.macos-arm64/\" target=\"_blank\" rel=\"noreferrer\">JetBrains.dotCover.CommandLineTools.macos-arm64.&lt;VERSION&gt;.nupkg</a>" +
            "<br/><br/>Supported tools for global tool:" +
            "<br/><a href=\"https://www.nuget.org/packages/JetBrains.dotCover.GlobalTool/\" target=\"_blank\" rel=\"noreferrer\">JetBrains.dotCover.GlobalTool.&lt;VERSION&gt;.nupkg</a>";
}