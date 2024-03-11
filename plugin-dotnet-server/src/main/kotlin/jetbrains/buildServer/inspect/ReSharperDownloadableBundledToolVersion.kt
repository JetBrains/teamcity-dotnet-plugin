package jetbrains.buildServer.inspect

import jetbrains.buildServer.tools.DownloadableBundledToolVersion
import jetbrains.buildServer.tools.ToolInstallationMode
import jetbrains.buildServer.tools.ToolType

class ReSharperDownloadableBundledToolVersion(
    toolType: ToolType,
    version: String
) : ReSharperToolVersion(toolType, version), DownloadableBundledToolVersion {

    override fun installationMode(): ToolInstallationMode = ToolInstallationMode.INSTALL
}