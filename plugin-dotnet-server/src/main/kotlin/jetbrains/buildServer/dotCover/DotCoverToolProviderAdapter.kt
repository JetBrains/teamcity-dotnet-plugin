package jetbrains.buildServer.dotCover

import jetbrains.buildServer.ToolService
import jetbrains.buildServer.dotnet.DotnetConstants.DOTCOVER_PACKAGE_TYPE
import jetbrains.buildServer.dotnet.DotnetConstants.DOTCOVER_WIN_PACKAGE_TYPE
import jetbrains.buildServer.tools.ServerToolProviderAdapter
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

class DotCoverToolProviderAdapter(
        private val _toolService: ToolService,
        private val _toolType: ToolType)
    : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService.getTools(getType(), DOTCOVER_PACKAGE_TYPE, DOTCOVER_WIN_PACKAGE_TYPE).toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(getType(), toolPackage) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(getType(), toolVersion, targetDirectory)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(getType(), toolPackage, "tools/", targetDirectory)

    override fun getDefaultBundledVersionId(): String? = null
}