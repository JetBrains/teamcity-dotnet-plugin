

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