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