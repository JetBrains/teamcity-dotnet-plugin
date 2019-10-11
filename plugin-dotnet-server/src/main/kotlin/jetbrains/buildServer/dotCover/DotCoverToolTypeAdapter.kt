package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.tools.ToolTypeAdapter

class DotCoverToolTypeAdapter : ToolTypeAdapter() {
    override fun getType()= DotnetConstants.DOTCOVER_PACKAGE_TYPE

    override fun getDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_TOOL_TYPE_NAME

    override fun getDescription(): String? = "Is used in JetBrains dotCover-specific build steps to get code coverage."

    override fun getShortDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_SHORT_TOOL_TYPE_NAME

    override fun getTargetFileDisplayName() = DotnetConstants.DOTCOVER_PACKAGE_TARGET_FILE_DISPLAY_NAME

    override fun isSupportDownload() = true

    override fun getToolSiteUrl() = "https://www.jetbrains.com/dotcover/download/#section=commandline"

    override fun getToolLicenseUrl() = "https://www.jetbrains.com/dotcover/download/command_line_license.html"

    override fun getTeamCityHelpFile() = "JetBrains+dotCover"

    override fun getValidPackageDescription() =  "Specify the path to a " + displayName + " (.nupkg).\n" +
                "<br/>Download <em>${DotnetConstants.DOTCOVER_PACKAGE_TYPE}.&lt;VERSION&gt;.nupkg</em> from\n" +
                "<a href=\"https://www.nuget.org/packages/${DotnetConstants.DOTCOVER_PACKAGE_TYPE}/\" target=\"_blank\" rel=\"noreferrer\">www.nuget.org</a>"

    companion object {
        internal val Shared: ToolTypeAdapter = DotCoverToolTypeAdapter()
    }
}