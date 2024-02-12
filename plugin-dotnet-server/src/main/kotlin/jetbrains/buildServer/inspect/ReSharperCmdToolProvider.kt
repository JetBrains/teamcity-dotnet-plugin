

package jetbrains.buildServer.inspect

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.*
import jetbrains.buildServer.inspect.CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.jdom.JDOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.IOException

class ReSharperCmdToolProvider(
        private val _packageId: String,
        private val _toolService: ToolService,
        private val _toolType: ToolType,
        private val _fileSystem: FileSystemService,
        private val _pluginDescriptor: PluginDescriptor,
        private val _xmlDocumentService: XmlDocumentService)
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

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        _toolService.unpackToolPackage(toolPackage, "", targetDirectory, _packageId, JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID)
        fixUnpackedToolPaths(targetDirectory)
        val pluginRoot = _pluginDescriptor.getPluginRoot();
        val toolXmlFileFrom = File(pluginRoot, "server/bundled-tools/JetBrains.ReSharper.CommandLineTool/bundled-tool.xml")
        val toolXmlFileTo = File(targetDirectory, "teamcity-plugin.xml")
        _fileSystem.copy(toolXmlFileFrom, toolXmlFileTo)
    }

    override fun getBundledToolVersions(): MutableCollection<InstalledToolVersion> {
        val pluginRoot = _pluginDescriptor.getPluginRoot();
        val bundledCltNuspecFile = File(pluginRoot, "server/bundled-tools/JetBrains.ReSharper.CommandLineTool/JetBrains.ReSharper.CommandLineTools.nuspec")
        if (!_fileSystem.isExists(bundledCltNuspecFile) || !_fileSystem.isFile(bundledCltNuspecFile)) {
            LOG.warn("Bundled ReSharper CLT nuspec file doesn't exist on path " + bundledCltNuspecFile.absolutePath)
            return super.getBundledToolVersions()
        }

        val bundledToolPackage = File(pluginRoot, "server/bundled-tools/JetBrains.ReSharper.CommandLineTool/jetbrains.resharper-clt.bundled.zip")
        if (!_fileSystem.isExists(bundledToolPackage) || !_fileSystem.isFile(bundledToolPackage)) {
            LOG.warn("Bundled ReSharper CLT tool package doesn't exist on path " + bundledToolPackage.absolutePath)
            return super.getBundledToolVersions()
        }

        val result = mutableListOf<InstalledToolVersion>()
        try {
            _fileSystem.read(bundledCltNuspecFile) {
                val doc = _xmlDocumentService.deserialize(it)
                getContents(doc, "/package/metadata/version")
                        .firstOrNull()
                        ?.let { bundledCltVersion ->
                            result.add(SimpleInstalledToolVersion(BundledToolVersion(_toolType, bundledCltVersion), null, null, bundledToolPackage))
                        }
            }
        } catch (error: Exception) {
            when (error) {
                is JDOMException,
                is IOException -> {
                    LOG.warn("Failed to get version of bundled CLT from file " + bundledCltNuspecFile.absolutePath, error)
                    return super.getBundledToolVersions()
                }
                else -> throw error
            }
        }

        return result
    }

    // Unpacked R# CLT files should reside in the /tools subdirectory of the tool directory.
    // This is already true for R# CLT .nupkg packages and must be fixed manually for R# CLT .zip packages.
    private fun fixUnpackedToolPaths(unpackedToolDirectory: File) {
        val targetDirectory = File(unpackedToolDirectory, "tools")
        if (targetDirectory.exists()) return

        // Rename the unpacked directory to a directory with a temp name and then rename it to the target name.
        // This is the simplest way to move all directory contents into a subdirectory.
        val tempToolDirectory = File(unpackedToolDirectory.absolutePath + "_temp")
        FileUtil.rename(unpackedToolDirectory, tempToolDirectory)
        FileUtil.rename(tempToolDirectory, targetDirectory)
    }

    private fun getContents(doc: Document, xpath: String): Sequence<String> =
            doc.find<Element>(xpath).map { it.textContent }.filter { !it.isNullOrBlank() }

    companion object {
        private val LOG: Logger = Logger.getInstance(ReSharperCmdToolProvider::class.java.name)
    }

    private class BundledToolVersion internal constructor(toolType: ToolType, version: String)
        : SimpleToolVersion(toolType, version, ToolVersionIdHelper.getToolId(JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID, "bundled"))
}