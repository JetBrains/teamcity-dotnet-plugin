package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.ArchiveUtil
import jetbrains.buildServer.util.TimeService
import java.io.File
import java.io.FileFilter
import java.net.URL

class DotnetToolProviderAdapter(
        private val _timeService: TimeService,
        private val _packageVersionParser: NuGetPackageVersionParser,
        private val _httpDownloader: HttpDownloader,
        private val _nuGetService: NuGetService,
        private val _fileSystemService: FileSystemService): ServerToolProviderAdapter() {

    override fun getType(): jetbrains.buildServer.tools.ToolType = DotnetToolTypeAdapter.Shared;

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> = tools.toMutableList()

    override fun tryGetPackageVersion(toolPackage: File): GetPackageVersionResult {
        LOG.info("Get package version for file \"$toolPackage\"")

        val versionResult = _packageVersionParser.tryParse(toolPackage.name)?.let {
            GetPackageVersionResult.version(DotnetToolVersion(it.toString()))
        } ?: GetPackageVersionResult.error("Failed to get version of " + toolPackage)

        LOG.info("Package version is \"${versionResult.toolVersion?.version ?: "null"}\"")
        return versionResult
    }

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File): File {
        LOG.info("Fetch package for version \"${toolVersion.version ?: "null"}\" to directory \"$targetDirectory\"")

        val downloadableTool = tools
                .filter { it.version == toolVersion.version }
                .firstOrNull()
                ?: throw ToolException("Failed to find package " + toolVersion)

        val downloadUrl = downloadableTool.downloadUrl
        LOG.info("Start installing package \"${toolVersion.displayName}\"")
        LOG.info("Downloading package from: \"$downloadUrl\"")
        val targetFile = File(targetDirectory, downloadableTool.destinationFileName)
        try {
            _fileSystemService.write(targetFile) {
                _httpDownloader.download(URL(downloadUrl), it)
            }

            LOG.info("Package from: \"$downloadUrl\" was downloaded to \"$targetFile\"")
            return targetFile
        } catch (e: Throwable) {
            throw ToolException("Failed to download package \"$toolVersion\" to \"$targetFile\": \"${e.message}\"", e)
        }
    }

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        LOG.info("Unpack package \"$toolPackage\" to directory \"$targetDirectory\"")

        if (NUGET_PACKAGE_FILE_FILTER.accept(toolPackage) && _packageVersionParser.tryParse(toolPackage.name) != null) {
            if (!ArchiveUtil.unpackZip(toolPackage, DotnetConstants.PACKAGE_BINARY_NUPKG_PATH + "/", targetDirectory)) {
                throw ToolException("Failed to unpack package $toolPackage to $targetDirectory")
            }

            LOG.info("Package \"$toolPackage\" was unpacked to directory \"$targetDirectory\"")
        }
        else {
            LOG.info("Package ${toolPackage} is not acceptable")
        }
    }

    private val tools: List<DotnetTool> get() {
        try {
            return _nuGetService.getPackagesById(type.type, true).filter { it.isListed == true }.map { DotnetTool(it) }.toList().reversed()
        } catch (e: Throwable) {
            throw ToolException("Failed to download list of packages for ${type.type}: " + e.message, e)
        }
    }

    private inner class DotnetToolVersion internal constructor(version: String)
        : SimpleToolVersion(type, version, ToolVersionIdHelper.getToolId(DotnetConstants.PACKAGE_TYPE, version))

    companion object {
        private val LOG: Logger = Logger.getInstance(DotnetToolProviderAdapter::class.java.name)
        private val NUGET_PACKAGE_FILE_FILTER = FileFilter { packageFile ->
            packageFile.isFile && packageFile.nameWithoutExtension.startsWith(DotnetConstants.PACKAGE_TYPE, true) && DotnetConstants.PACKAGE_NUGET_EXTENSION.equals(packageFile.extension, true)
        }
    }
}