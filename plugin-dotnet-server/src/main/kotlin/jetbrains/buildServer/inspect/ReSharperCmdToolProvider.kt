package jetbrains.buildServer.inspect

import jetbrains.buildServer.ToolService
import jetbrains.buildServer.tools.ServerToolProviderAdapter
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.tools.ToolVersion
import java.io.File

class ReSharperCmdToolProvider(
        private val _packageId: String,
        private val _toolService: ToolService,
        private val _toolType: ToolType)
    : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService
                    .getTools(type, _packageId)
                    .filter { it.version.startsWith("2") }
                    .toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(type, toolPackage, _packageId) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(type, toolVersion, targetDirectory, _packageId)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(toolPackage, "", targetDirectory, _packageId)

    override fun getDefaultBundledVersionId(): String? = null
}