package jetbrains.buildServer.dotCover

import jetbrains.buildServer.tools.DownloadableBundledToolVersion
import jetbrains.buildServer.tools.ToolInstallationMode
import jetbrains.buildServer.tools.ToolType

class DotCoverDownloadableBundledToolVersion(
    toolType: ToolType,
    version: String,
    packageId: String,
) : DotCoverToolVersion(toolType, version, packageId), DownloadableBundledToolVersion {

    override fun installationMode(): ToolInstallationMode = ToolInstallationMode.INSTALL
}