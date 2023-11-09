/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotCover

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.ToolService
import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_DEPRECATED_PACKAGE_ID
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_PACKAGE_ID
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.find
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.jdom.JDOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.IOException

class DotCoverToolProviderAdapter(
        private val _toolService: ToolService,
        private val _toolType: ToolType,
        private val _toolComparator: DotCoverToolComparator,
        private val _toolFilter: DotCoverPackageFilter,
        private val _packageIdResolver: DotCoverPackageIdResolver,
        private val _pluginDescriptor: PluginDescriptor,
        private val _fileSystem: FileSystemService,
        private val _xmlDocumentService: XmlDocumentService
) : ServerToolProviderAdapter() {

    override fun getType() = _toolType

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> = getPackages()

    override fun tryGetPackageVersion(toolPackage: File): GetPackageVersionResult {
        val version = _toolService.getPackageVersion(toolPackage, *DOT_COVER_PACKAGES)
            ?: return super.tryGetPackageVersion(toolPackage)
        val packageId = _packageIdResolver.resolvePackageId(toolPackage.name)
            ?: return super.tryGetPackageVersion(toolPackage)

        return GetPackageVersionResult.version(DotCoverToolVersion(_toolType, version.toString(), packageId))
    }

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File): File {
        LOG.debug("Fetch package for version \"${toolVersion.version}\" to directory \"$targetDirectory\"")

        val dotCoverToolVersion = getPackages()
            .firstOrNull { it.version == toolVersion.version }
            ?: throw ToolException("Failed to find package $toolVersion")

        val downloadUrl = dotCoverToolVersion.downloadUrl
        val destinationFileName = dotCoverToolVersion.destinationFileName

        return _toolService.fetchToolPackage(dotCoverToolVersion, targetDirectory, downloadUrl, destinationFileName)
    }

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        val pathPrefix = pathPrefix(toolPackage)
        _toolService.unpackToolPackage(toolPackage, pathPrefix, targetDirectory, *DOT_COVER_PACKAGES)

        val pluginRoot = _pluginDescriptor.pluginRoot
        val toolXmlFileFrom = File(pluginRoot, "server/bundled-tools/JetBrains.dotCover.CommandLineTool/bundled-dot-cover.xml")
        val toolXmlFileTo = File(targetDirectory, "teamcity-plugin.xml")
        _fileSystem.copy(toolXmlFileFrom, toolXmlFileTo)
    }

    override fun getUnpackedPath(toolPackage: File, sourcePath: String): String? {
        val pathPrefix = pathPrefix(toolPackage)
        if (pathPrefix.isEmpty()) {
            return sourcePath;
        } else if (sourcePath.startsWith(pathPrefix)) {
            return sourcePath.removePrefix(pathPrefix)
        }
        return null;
    }

    private fun pathPrefix(toolPackage: File) =
        if (toolPackage.name.lowercase().endsWith(DotnetConstants.PACKAGE_NUGET_EXTENSION)) "tools/" else ""

    override fun getDefaultBundledVersionId(): String? = null

    override fun getBundledToolVersions(): Collection<InstalledToolVersion> {
        val pluginRoot: File = _pluginDescriptor.pluginRoot
        val dotcoverBundledNuspecFilePath = TeamCityProperties.getProperty(
            "teamcity.dotCover.bundled.nuspec", CoverageConstants.DOTCOVER_BUNDLED_NUSPEC_FILE_PATH)
        val dotcoverBundledAgentToolPackagePath = TeamCityProperties.getProperty(
            "teamcity.dotCover.bundled.tool.package", CoverageConstants.DOTCOVER_BUNDLED_AGENT_TOOL_PACKAGE_PATH)
        val bundledNuspecFile = File(pluginRoot, dotcoverBundledNuspecFilePath)
        val bundledToolPackage = File(pluginRoot, dotcoverBundledAgentToolPackagePath)

        if (!bundledNuspecFile.isFile) {
            LOG.warn("Bundled dotCover nuspec file doesn't exist on path " + bundledNuspecFile.absolutePath)
            return super.getBundledToolVersions()
        }
        if (!bundledToolPackage.isFile) {
            LOG.warn("Bundled dotCover tool package doesn't exist on path " + bundledToolPackage.absolutePath)
            return super.getBundledToolVersions()
        }

        val result = mutableSetOf<InstalledToolVersion>()
        try {
            _fileSystem.read(bundledNuspecFile) {
                val doc = _xmlDocumentService.deserialize(it)
                val bundledPackageVersion = getContents(doc, "/package/metadata/version").firstOrNull()
                val bundledPackageId = getContents(doc, "/package/metadata/id").firstOrNull()

                if (bundledPackageVersion != null && bundledPackageId != null) {
                    result.add(SimpleInstalledToolVersion(
                        DotCoverToolVersion(_toolType, bundledPackageVersion, bundledPackageId, true),
                        null,
                        null,
                        bundledToolPackage))
                }
            }
        } catch (error: Exception) {
            when (error) {
                is JDOMException,
                is IOException -> {
                    LOG.warn("Failed to get version of bundled CLT from file " + bundledNuspecFile.absolutePath, error)
                    return super.getBundledToolVersions()
                }
                else -> throw error
            }
        }

        return result
    }

    private fun getPackages() = _toolService.getPackages(*DOT_COVER_PACKAGES)
        .filter { _toolFilter.accept(it) }
        .map { DotCoverDownloadableToolVersion(_toolType, it) }
        .sortedWith(_toolComparator)
        .toMutableList()

    companion object {
        private val LOG: Logger = Logger.getInstance(DotCoverToolProviderAdapter::class.java.name)
        private val DOT_COVER_PACKAGES = arrayOf(DOTCOVER_DEPRECATED_PACKAGE_ID, DOTCOVER_PACKAGE_ID)

        fun getContents(doc: Document, xpath: String): Sequence<String> =
            doc.find<Element>(xpath).map { it.textContent }.filter { !it.isNullOrBlank() }
    }
}