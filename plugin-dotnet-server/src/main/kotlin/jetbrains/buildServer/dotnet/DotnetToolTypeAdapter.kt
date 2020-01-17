/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants.INTEGRATION_PACKAGE_TYPE
import jetbrains.buildServer.tools.ToolTypeAdapter

class DotnetToolTypeAdapter : ToolTypeAdapter() {
    override fun getType() = DotnetConstants.INTEGRATION_PACKAGE_TYPE

    override fun getDisplayName() = DotnetConstants.INTEGRATION_PACKAGE_TOOL_TYPE_NAME

    override fun getDescription(): String? = "Is used in .NET CLI build steps."

    override fun getShortDisplayName() = DotnetConstants.INTEGRATION_PACKAGE_SHORT_TOOL_TYPE_NAME

    override fun getTargetFileDisplayName() = DotnetConstants.INTEGRATION_PACKAGE_TARGET_FILE_DISPLAY_NAME

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://github.com/JetBrains/TeamCity.MSBuild.Logger/"

    override fun getToolLicenseUrl() =  "https://github.com/JetBrains/TeamCity.MSBuild.Logger/blob/master/LICENSE"

    override fun getValidPackageDescription(): String? =
        "Specify the path to a " + displayName + " (.nupkg).\n" +
                "<br/>Download <em>${INTEGRATION_PACKAGE_TYPE}.&lt;VERSION&gt;.nupkg</em> from\n" +
                "<a href=\"https://www.nuget.org/packages/${INTEGRATION_PACKAGE_TYPE}/\" target=\"_blank\" rel=\"noreferrer\">www.nuget.org</a>"

    companion object {
        internal val Shared: ToolTypeAdapter = DotnetToolTypeAdapter()
    }
}