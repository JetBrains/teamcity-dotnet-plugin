package jetbrains.buildServer

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion

class NuGetTool(
        private val _toolType: ToolType,
        private val _package: NuGetPackage) : ToolVersion {

    val downloadUrl get() = _package.downloadUrl.toString()

    val destinationFileName get() = "${_package.packageId}.${_package.packageVersion}.${DotnetConstants.PACKAGE_NUGET_EXTENSION}"

    override fun getType() = _toolType

    override fun getVersion() =_package.packageVersion.toString()

    override fun getId() = _toolType.type + "." + _package.packageVersion

    override fun getDisplayName() = _toolType.type + version
}