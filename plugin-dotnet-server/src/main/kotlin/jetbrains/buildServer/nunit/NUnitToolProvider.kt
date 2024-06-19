package jetbrains.buildServer.nunit

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.HttpDownloader
import jetbrains.buildServer.tools.*
import jetbrains.buildServer.tools.available.*
import jetbrains.buildServer.util.ArchiveUtil
import jetbrains.buildServer.util.TimeService
import java.io.*
import java.net.URL

class NUnitToolProvider(
    timeService: TimeService,
    availableToolsFetcher: AvailableToolsFetcher,
    private val _httpDownloader: HttpDownloader,
    private val _fileSystem: FileSystemService
) : ServerToolProviderAdapter() {
    private val _availableTools: AvailableToolsState =
        AvailableToolsStateImpl(timeService, listOf(availableToolsFetcher))

    override fun getType() = CLT_TOOL_TYPE

    override fun getAvailableToolVersions(): Collection<DownloadableToolVersion> =
        _availableTools.getAvailable(FetchToolsPolicy.FetchNew).fetchedTools

    override fun tryGetPackageVersion(toolPackage: File): GetPackageVersionResult {
        val packageName = toolPackage.name
        val toolVersion: String = NUnitToolVersion.getPackageVersion(packageName)
        if (toolVersion.isEmpty()) {
            return GetPackageVersionResult.error("Failed to get NUnit version of $packageName")
        }

        return GetPackageVersionResult.version(ToolVersion(toolVersion))
    }

    override fun fetchToolPackage(toolVersion: jetbrains.buildServer.tools.ToolVersion, targetDirectory: File): File {
        val downloadableTool = _availableTools.getAvailable(FetchToolsPolicy.ReturnCached)
            .fetchedTools
            .firstOrNull { data: DownloadableToolVersion -> data.version == toolVersion.version }
        if (downloadableTool == null) {
            throw ToolException("Failed to fetch tool $toolVersion. Download source info wasn't prefetched.")
        }

        val downloadUrl = downloadableTool.downloadUrl
        LOG.info("Start installing package " + toolVersion.displayName)
        LOG.info("Downloading package from: $downloadUrl")
        val targetFile = File(targetDirectory, downloadableTool.destinationFileName)
        try {
            _fileSystem.write(targetFile) { fileStream ->
                _httpDownloader.download(URL(downloadUrl), fileStream)
                fileStream.flush()
            }
        } catch (e: Throwable) {
            throw ToolException("Failed to download package " + toolVersion + " to " + targetFile + e.message, e)
        }

        LOG.debug("Successfully downloaded package $toolVersion to $targetFile")
        return targetFile
    }

    override fun unpackToolPackage(toolPackage: File, targetDirectory: File) {
        if (!ArchiveUtil.unpackZip(toolPackage, "", targetDirectory)) {
            throw ToolException("Failed to unpack NUnit package $toolPackage to $targetDirectory")
        }
    }

    private inner class ToolVersion(version: String) : SimpleToolVersion(
        CLT_TOOL_TYPE,
        version,
        ToolVersionIdHelper.getToolId(
            NUnitRunnerConstants.NUNIT_TOOL_TYPE_ID,
            version
        )
    )

    companion object {
        private val LOG = Logger.getInstance(NUnitToolProvider::class.java.name)
        internal val CLT_TOOL_TYPE: ToolType = object : ToolTypeAdapter() {
            override fun getType() = NUnitRunnerConstants.NUNIT_TOOL_TYPE_ID
            override fun getDisplayName() = NUnitRunnerConstants.NUNIT_TOOL_TYPE_NAME
            override fun getDescription() = "Is used in the TeamCity NUnit build runner to run tests."
            override fun getShortDisplayName() = NUnitRunnerConstants.NUNIT_TOOL_TYPE_NAME
            override fun getTargetFileDisplayName() = "NUnit console tool including the file name"
            override fun isSupportDownload() = true
            override fun getToolSiteUrl() = "https://github.com/nunit/nunit-console/releases"
            override fun getToolLicenseUrl() = "https://docs.nunit.org/articles/nunit/license.html"
            override fun getTeamCityHelpFile() = "NUnit"
            override fun getValidPackageDescription() = """Specify the path to a $displayName (.zip).
<br/>Download <em>NUnit.Console-&lt;VERSION&gt;.zip</em> from
<a href="https://github.com/nunit/nunit-console/releases" target="_blank" rel="noreferrer">nunit-console releases</a>"""
        }
    }
}

