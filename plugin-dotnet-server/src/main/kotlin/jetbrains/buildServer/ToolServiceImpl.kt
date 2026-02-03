

package jetbrains.buildServer

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.NuGetPackageVersion
import jetbrains.buildServer.dotnet.NuGetPackageVersionParser
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.ArchiveUtil
import java.io.File
import java.io.FileFilter
import java.net.URL

class ToolServiceImpl(
    private val _packageVersionParser: NuGetPackageVersionParser,
    private val _httpDownloader: HttpDownloader,
    private val _nuGetService: NuGetService,
    private val _fileSystemService: FileSystemService
) : ToolService {

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
            val errorMessage =
                "Failed to download the list of tool packages for ${toolType.displayName}. " +
                        "This may occur due to limited access to api.nuget.org."
            throw ToolException(errorMessage, e)
        }
    }

    override fun getPackages(vararg packageIds: String): List<NuGetPackage> {
        try {
            return packageIds
                .asSequence()
                .flatMap { _nuGetService.getPackagesById(it) }
                .toList()
                .reversed()
        } catch (e: Throwable) {
            val errorMessage =
                "Failed to download the list of tool packages. This may occur due to limited access to api.nuget.org."
            throw ToolException(errorMessage, e)
        }
    }

    override fun tryGetPackageVersion(toolType: ToolType, toolPackage: File, vararg packageIds: String): GetPackageVersionResult? {
        if (!toolPackage.isPackageFileValid(packageIds)) {
            return null
        }

        LOG.debug("Get package version for file \"$toolPackage\"")
        val versionResult = _packageVersionParser.tryParse(toolPackage.name)
            ?.let { GetPackageVersionResult.version(SimpleToolVersion(toolType, it.toString(), ToolVersionIdHelper.getToolId(toolType.type, it.toString()))) }
            ?: GetPackageVersionResult.error("Failed to get version of $toolPackage")

        LOG.debug("Package version is \"${versionResult.toolVersion?.version ?: "null"}\"")
        return versionResult
    }

    override fun getPackageVersion(toolPackage: File, vararg packageIds: String): NuGetPackageVersion? {
        if (!toolPackage.isPackageFileValid(packageIds)) {
            return null
        }

        LOG.debug("Get package version for file \"$toolPackage\"")
        return _packageVersionParser.tryParse(toolPackage.name)
    }

    override fun fetchToolPackage(toolType: ToolType, toolVersion: ToolVersion, targetDirectory: File, vararg packageIds: String): File {
        LOG.debug("Fetch package for version \"${toolVersion.version}\" to directory \"$targetDirectory\"")

        val downloadableTool = getTools(toolType, *packageIds)
            .firstOrNull { it.version == toolVersion.version && it.id == toolVersion.id }
            ?: throw ToolException("Failed to find package $toolVersion")

        return fetchToolPackage(toolVersion, targetDirectory, downloadableTool.downloadUrl, downloadableTool.destinationFileName)
    }

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File, downloadUrl: String, destinationFileName: String): File {
        LOG.debug("Start installing package \"${toolVersion.displayName}\" from: \"$downloadUrl\"")
        val targetFile = File(targetDirectory, destinationFileName)
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

    override fun unpackToolPackage(toolPackage: File, packageDirectory: String, targetDirectory: File, vararg packageIds: String) {
        LOG.debug("Unpack package \"$toolPackage\" to directory \"$targetDirectory\"")
        when {
            toolPackage.isPackageFileValid(packageIds) && isPackageVersionValid(toolPackage) -> {
                when (ArchiveUtil.unpackZip(toolPackage, packageDirectory, targetDirectory)) {
                    true -> LOG.debug("Package \"$toolPackage\" was unpacked to directory \"$targetDirectory\"")
                    false -> throw ToolException("Failed to unpack package $toolPackage to $targetDirectory")
                }
            }
            else -> LOG.debug("Package $toolPackage is not acceptable")
        }
    }

    private fun File.isPackageFileValid(packageIds: Array<out String>) =
        FileFilter { packageFile ->
            packageFile.isFile
                && packageIds.toSet().any { packageFile.name.startsWith(it, true) }
                && AllowedPackageExtensions.any { packageFile.name.lowercase().endsWith(it) }
        }.accept(this)

    private fun isPackageVersionValid(toolPackage: File) = _packageVersionParser.tryParse(toolPackage.name) != null

    companion object {
        private val LOG: Logger = Logger.getInstance(ToolServiceImpl::class.java.name)
        private val AllowedPackageExtensions = arrayOf(
            ".${DotnetConstants.PACKAGE_NUGET_EXTENSION}",
            ".zip",
            ".tar.gz",
        )
    }
}