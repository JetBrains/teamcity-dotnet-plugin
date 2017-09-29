package jetbrains.buildServer.dotnet

import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import org.apache.xmlrpc.XmlRpc.version

class DotnetTool(
        private val _package: NuGetPackage): ToolVersion {

    val downloadUrl: String
        get() {
            return _package.downloadUrl.toString()
        }

    val destinationFileName: String
        get() {
            return "${_package.packageId}.${_package.packageVersion}.${DotnetConstants.PACKAGE_NUGET_EXTENSION}"
        }

    override fun getType(): ToolType {
        return DotnetToolTypeAdapter.Shared
    }

    override fun getVersion(): String {
        return _package.packageVersion.toString()
    }

    override fun getId(): String {
        return DotnetToolTypeAdapter.Shared.getType() + "." + _package.packageVersion
    }

    override fun getDisplayName(): String {
        return DotnetToolTypeAdapter.Shared.getType() + version
    }
}