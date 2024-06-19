package jetbrains.buildServer.nunit

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.HttpDownloader
import jetbrains.buildServer.tools.available.AvailableToolsFetcher
import jetbrains.buildServer.tools.available.FetchAvailableToolsResult
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class NUnitAvailableToolsFetcher(
    private val _httpDownloader: HttpDownloader,
    private val _releasesParser: ToolsParser
) : AvailableToolsFetcher {
    override fun fetchAvailable(): FetchAvailableToolsResult {
        try {
            val releasesStream = ByteArrayOutputStream()
            _httpDownloader.download(URL(NUNIT_URL), releasesStream)
            val tools = _releasesParser.parse(InputStreamReader(ByteArrayInputStream(releasesStream.toByteArray())))
            if (tools.isEmpty()) {
                return FetchAvailableToolsResult.createError("Failed to fetch versions from: $NUNIT_URL")
            }

            return FetchAvailableToolsResult.createSuccessful(tools)
        } catch (e: IOException) {
            LOG.debug(e)
            return FetchAvailableToolsResult.createError("Failed to fetch versions from: $NUNIT_URL", e)
        }
    }

    companion object {
        private const val NUNIT_URL: String = "https://api.github.com/repos/nunit/nunit-console/releases"
        private val LOG = Logger.getInstance(NUnitAvailableToolsFetcher::class.java.name)
    }
}