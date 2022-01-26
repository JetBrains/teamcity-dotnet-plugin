/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.SemanticVersionParser
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.ArchiveUtil
import java.io.File
import java.io.FileFilter
import java.net.URL

class ToolServiceImpl(
        private val _packageVersionParser: SemanticVersionParser,
        private val _httpDownloader: HttpDownloader,
        private val _nuGetService: NuGetService,
        private val _fileSystemService: FileSystemService)
    : ToolService {

    override fun getTools(toolType: ToolType, vararg packageIds: String): List<NuGetTool> {
        try {
            return packageIds
                    .asSequence()
                    .flatMap { _nuGetService.getPackagesById(it) }
                    .map { NuGetTool(toolType, it) }
                    .sortedBy { it.version + ":" + it.id }
                    .toList()
                    .reversed()
        } catch (e: Throwable) {
            throw ToolException("Failed to download list of packages for ${toolType.type}: " + e.message, e)
        }
    }

    override fun tryGetPackageVersion(toolType: ToolType, toolPackage: File, vararg packageIds: String): GetPackageVersionResult? {
        if (!createPackageFilter(packageIds.toSet()).accept(toolPackage)) {
            return null
        }

        LOG.debug("Get package version for file \"$toolPackage\"")
        val versionResult = _packageVersionParser.tryParse(toolPackage.name)?.let {
            GetPackageVersionResult.version(SimpleToolVersion(toolType, it.toString(), ToolVersionIdHelper.getToolId(toolType.type, it.toString())))
        } ?: GetPackageVersionResult.error("Failed to get version of $toolPackage")

        LOG.debug("Package version is \"${versionResult.toolVersion?.version ?: "null"}\"")
        return versionResult
    }

    override fun fetchToolPackage(toolType: ToolType, toolVersion: ToolVersion, targetDirectory: File, vararg packageIds: String): File {
        LOG.debug("Fetch package for version \"${toolVersion.version}\" to directory \"$targetDirectory\"")

        val downloadableTool = getTools(toolType, *packageIds).firstOrNull { it.version == toolVersion.version && it.id == toolVersion.id }
                ?: throw ToolException("Failed to find package $toolVersion")

        val downloadUrl = downloadableTool.downloadUrl
        LOG.debug("Start installing package \"${toolVersion.displayName}\" from: \"$downloadUrl\"")
        val targetFile = File(targetDirectory, downloadableTool.destinationFileName)
        try {
            _fileSystemService.write(targetFile) {
                _httpDownloader.download(URL(downloadUrl), it)
            }

            LOG.debug("Package from: \"$downloadUrl\" was downloaded to \"$targetFile\"")
            return targetFile
        } catch (e: Throwable) {
            throw ToolException("Failed to download package \"$toolVersion\" to \"$targetFile\": \"${e.message}\"", e)
        }
    }

    override fun unpackToolPackage(toolPackage: File, nugetPackageDirectory: String, targetDirectory: File, vararg packageIds: String) {
        LOG.debug("Unpack package \"$toolPackage\" to directory \"$targetDirectory\"")

        if (createPackageFilter(packageIds.toSet()).accept(toolPackage) && _packageVersionParser.tryParse(toolPackage.name) != null) {
            if (!ArchiveUtil.unpackZip(toolPackage, nugetPackageDirectory, targetDirectory)) {
                throw ToolException("Failed to unpack package $toolPackage to $targetDirectory")
            }

            LOG.debug("Package \"$toolPackage\" was unpacked to directory \"$targetDirectory\"")
        } else {
            LOG.debug("Package $toolPackage is not acceptable")
        }
    }

    private fun createPackageFilter(packageIds: Set<String>) =
            FileFilter { packageFile ->
                packageFile.isFile
                        && packageIds.any { packageFile.nameWithoutExtension.startsWith(it, true) }
                        && DotnetConstants.PACKAGE_NUGET_EXTENSION.equals(packageFile.extension, true)
            }

    companion object {
        private val LOG: Logger = Logger.getInstance(ToolServiceImpl::class.java.name)
    }
}