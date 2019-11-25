package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.ToolService
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.web.openapi.PluginDescriptor
import java.io.File
import java.io.FileFilter

class DotnetToolProviderAdapter(
        private val _toolService: ToolService,
        private val _pluginDescriptor: PluginDescriptor,
        private val _packageVersionParser: SemanticVersionParser,
        private val _fileSystemService: FileSystemService) : ServerToolProviderAdapter() {

    override fun getType(): jetbrains.buildServer.tools.ToolType = DotnetToolTypeAdapter.Shared

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> =
            _toolService.getTools(DotnetToolTypeAdapter.Shared, DotnetToolTypeAdapter.Shared.type).toMutableList()

    override fun tryGetPackageVersion(toolPackage: File) =
            _toolService.tryGetPackageVersion(DotnetToolTypeAdapter.Shared, toolPackage) ?: super.tryGetPackageVersion(toolPackage)

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File) =
            _toolService.fetchToolPackage(DotnetToolTypeAdapter.Shared, toolVersion, targetDirectory)

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) =
            _toolService.unpackToolPackage(DotnetToolTypeAdapter.Shared, toolPackage, "build/_common/", targetDirectory)

    override fun getDefaultBundledVersionId(): String? = null

    override fun getBundledToolVersions(): MutableCollection<InstalledToolVersion> {
        val pluginPath = File(_pluginDescriptor.pluginRoot, "server")

        val toolPackage = _fileSystemService
                .list(pluginPath)
                .filter { NUGET_BUNDLED_FILTER.accept(it) }
                .firstOrNull()

        if (toolPackage == null) {
            LOG.warn("Failed to find package spec on path $pluginPath")
            return super.getBundledToolVersions()
        }

        val toolVersion = _packageVersionParser.tryParse(toolPackage.nameWithoutExtension)
                ?.let { GetPackageVersionResult.version(DotnetToolVersion(it.toString())).toolVersion }

        if (toolVersion == null) {
            LOG.warn("Failed to parse version from \"${toolPackage.nameWithoutExtension}\"")
            return super.getBundledToolVersions()
        }

        return mutableListOf(SimpleInstalledToolVersion.newBundledToAgentTool(DotnetToolVersion(toolVersion.version), toolPackage))
    }

    private inner class DotnetToolVersion internal constructor(version: String)
        : SimpleToolVersion(type, version, ToolVersionIdHelper.getToolId(DotnetConstants.INTEGRATION_PACKAGE_TYPE, version))

    companion object {
        private val LOG: Logger = Logger.getInstance(DotnetToolProviderAdapter::class.java.name)

        private val NUGET_BUNDLED_FILTER = FileFilter { packageFile ->
            packageFile.isFile && packageFile.nameWithoutExtension.startsWith(DotnetConstants.INTEGRATION_PACKAGE_TYPE, true) && "jar".equals(packageFile.extension, true)
        }
    }
}