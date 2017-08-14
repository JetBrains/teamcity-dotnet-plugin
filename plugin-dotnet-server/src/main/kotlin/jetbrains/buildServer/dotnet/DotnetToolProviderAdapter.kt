package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.util.ArchiveUtil
import jetbrains.buildServer.util.CollectionsUtil
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

    override fun getType(): ToolType {
        return DotnetToolTypeAdapter.Shared;
    }

    override fun getAvailableToolVersions(): MutableCollection<out ToolVersion> {
        return tools.toMutableList()
    }

    override fun tryGetPackageVersion(toolPackage: File): GetPackageVersionResult {
        val packageVersion = _packageVersionParser.tryParse(toolPackage.name)
        if (packageVersion == null) {
            return GetPackageVersionResult.error("Failed to get version of " + toolPackage)
        }

        return GetPackageVersionResult.version(DotnetToolVersion(packageVersion.toString()))
    }

    override fun fetchToolPackage(toolVersion: ToolVersion, targetDirectory: File): File {
        val downloadableTool = tools
                .filter { it.version == toolVersion.version }
                .firstOrNull()
                ?: throw ToolException("Failed to find package " + toolVersion)

        val downloadUrl = downloadableTool.downloadUrl
        LOG.info("Start installing package " + toolVersion.displayName)
        LOG.info("Downloading package from: " + downloadUrl)
        val targetFile = File(targetDirectory, downloadableTool.destinationFileName)
        try {
            _fileSystemService
                    .createOutputFile(targetFile)
                    .use({
                        fileStream -> _httpDownloader.download(URL(downloadUrl), fileStream)
                        fileStream.flush() })
        } catch (e: Throwable) {
            throw ToolException("Failed to download package " + toolVersion + " to " + targetFile + e.message, e)
        }

        return targetFile
    }

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        var pathPrefix = ""
        if (NUGET_PACKAGE_FILE_FILTER.accept(toolPackage) && _packageVersionParser.tryParse(toolPackage.name) != null) {
            pathPrefix = DotnetConstants.PACKAGE_BINARY_NUPKG_PATH + "/"
        }

        if (!ArchiveUtil.unpackZip(toolPackage, pathPrefix, targetDirectory)) {
            throw ToolException("Failed to unpack package $toolPackage to $targetDirectory")
        }
    }

    private val tools: List<DotnetTool> get() {
        var listOfPackages: List<DotnetTool>
        try {
            listOfPackages = _nuGetService.getPackagesById(type.type, true).filter { it.isListed == true }.map { DotnetTool(it) }.toList()
        } catch (e: Throwable) {
            throw ToolException("Failed to download list of packages for ${type.type}: " + e.message, e)
        }

        return listOfPackages
    }

    private inner class DotnetToolVersion internal constructor(version: String)
        : SimpleToolVersion(
            type,
            version,
            ToolVersionIdHelper.getToolId(DotnetConstants.PACKAGE_TYPE, version))

    companion object {
        private val LOG: Logger = Logger.getInstance(DotnetToolProviderAdapter::class.java.name)
        private val NUGET_PACKAGE_FILE_FILTER = FileFilter { pathname ->
            val name = pathname.name
            pathname.isFile && name.startsWith(DotnetConstants.PACKAGE_TYPE) && name.endsWith(DotnetConstants.PACKAGE_NUGET_EXTENSION)
        }
    }
}