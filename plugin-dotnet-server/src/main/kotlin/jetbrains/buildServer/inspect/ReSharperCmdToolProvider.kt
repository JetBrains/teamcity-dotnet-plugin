package jetbrains.buildServer.inspect

import jetbrains.buildServer.ToolService
import jetbrains.buildServer.inspect.CltConstants.CLT_PACKAGE_ID
import jetbrains.buildServer.tools.ServerToolProviderAdapter
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

class ReSharperCmdToolProvider(
        private val _toolService: ToolService,
        private val _toolType: ToolType)
    : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService
                    .getTools(type, CLT_PACKAGE_ID)
                    .filter { it.version.startsWith("2") }
                    .toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(type, toolPackage) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(type, toolVersion, targetDirectory, CLT_PACKAGE_ID)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(type, toolPackage, "tools/", targetDirectory)

    override fun getDefaultBundledVersionId(): String? = null
}