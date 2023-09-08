package jetbrains.buildServer.dotCover

import jetbrains.buildServer.NuGetPackage
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.DownloadableToolVersion

class DotCoverDownloadableToolVersion(
    toolType: ToolType,
    private val _source: NuGetPackage
) : DotCoverToolVersion(toolType, _source.packageVersion, _source.packageId), DownloadableToolVersion {

    override fun getDownloadUrl(): String {
        return _source.downloadUrl.toString()
    }

    override fun getDestinationFileName(): String {
        return "${_source.packageId}.${_source.packageVersion}.${DotnetConstants.PACKAGE_NUGET_EXTENSION}"
    }
}
