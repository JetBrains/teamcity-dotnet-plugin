package jetbrains.buildServer.dotnet

import jetbrains.buildServer.tools.ToolTypeAdapter

class DotnetToolTypeAdapter : ToolTypeAdapter() {
    override fun getType(): String {
        return DotnetConstants.PACKAGE_TYPE
    }

    override fun getDisplayName(): String {
        return DotnetConstants.PACKAGE_TOOL_TYPE_NAME
    }

    override fun getDescription(): String? {
        return "Is used in .NET CLI build steps."
    }

    override fun getShortDisplayName(): String {
        return DotnetConstants.PACKAGE_SHORT_TOOL_TYPE_NAME
    }

    override fun getTargetFileDisplayName(): String {
        return DotnetConstants.PACKAGE_TARGET_FILE_DISPLAY_NAME
    }

    override fun isSupportDownload(): Boolean {
        return true
    }

    override fun getToolSiteUrl(): String {
        return "https://github.com/JetBrains/TeamCity.MSBuild.Logger/"
    }

    override fun getToolLicenseUrl(): String {
        return "https://github.com/JetBrains/TeamCity.MSBuild.Logger/blob/master/LICENSE"
    }

    override fun getValidPackageDescription(): String? {
        return "Specify the path to a " + displayName + " (.nupkg).\n" +
                "<br/>Download <em>TeamCity.Dotnet.Integration.&lt;VERSION&gt;.nupkg</em> from\n" +
                "<a href=\"https://www.nuget.org/packages/TeamCity.Dotnet.Integration/\" target=\"_blank\" rel=\"noreferrer\">www.nuget.org</a>"
    }

    companion object {
        internal val Shared: ToolTypeAdapter = DotnetToolTypeAdapter()
    }
}