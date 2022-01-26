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

package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.tools.ToolTypeAdapter

class DotCoverToolTypeAdapter : ToolTypeAdapter() {
    override fun getType()= DotnetConstants.DOTCOVER_PACKAGE_TYPE

    override fun getDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_TOOL_TYPE_NAME

    override fun getDescription(): String = "Is used in JetBrains dotCover-specific build steps to get code coverage."

    override fun getShortDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_SHORT_TOOL_TYPE_NAME

    override fun getTargetFileDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_TARGET_FILE_DISPLAY_NAME

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://www.jetbrains.com/legal/docs/toolbox/user.html"

    override fun getToolLicenseUrl() = "https://www.jetbrains.com/dotcover/download/command_line_license.html"

    override fun getTeamCityHelpFile() = "JetBrains+dotCover"

    override fun getValidPackageDescription() =  "Specify the path to a " + displayName + " (.nupkg).\n" +
                "<br/>Download <em>${DotnetConstants.DOTCOVER_PACKAGE_TYPE}.&lt;VERSION&gt;.nupkg</em> from\n" +
                "<a href=\"https://www.nuget.org/packages/${DotnetConstants.DOTCOVER_PACKAGE_TYPE}/\" target=\"_blank\" rel=\"noreferrer\">www.nuget.org</a>"
}