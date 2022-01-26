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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_NAME
import jetbrains.buildServer.tools.ToolTypeAdapter

class ReSharperCmdTypeAdapter : ToolTypeAdapter() {
    override fun getType()= JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID

    override fun getDisplayName() = JETBRAINS_RESHARPER_CLT_TOOL_TYPE_NAME

    override fun getDescription(): String = "Is used by Inspections (ReSharper), Duplicates Finder (ReSharper) build runners to run code analysis."

    override fun getShortDisplayName() = "R# CLT"

    override fun getTargetFileDisplayName() = "R# CLT Home Directory"

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://www.jetbrains.com/resharper/download/#section=commandline"

    override fun getToolLicenseUrl() = "https://www.jetbrains.com/legal/docs/resharper/resharper_clt_license.html"

    override fun getValidPackageDescription() =
            "Specify the path to a ReSharper Command Line Tools package (.zip or .nupkg).\n" +
            "<br/>Download <em>JetBrains.ReSharper.CommandLineTools.&lt;VERSION&gt;.zip</em> from\n" +
            "<a href=\"https://www.jetbrains.com/resharper/download/index.html#section=resharper-clt\" target=\"_blank\" rel=\"noreferrer\">jetbrains.com/resharper</a>" +
            "<br/>Download <em>JetBrains.ReSharper.CommandLineTools.&lt;VERSION&gt;.nupkg</em> from <a href=\"https://www.nuget.org/packages/JetBrains.ReSharper.CommandLineTools/\" target=\"_blank\" rel=\"noreferrer\">nuget.org</a>"
}