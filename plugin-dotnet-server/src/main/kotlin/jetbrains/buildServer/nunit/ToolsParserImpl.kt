package jetbrains.buildServer.nunit

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.tools.available.DownloadableToolVersion
import java.io.Reader

class ToolsParserImpl : ToolsParser {
    override fun parse(json: Reader): List<DownloadableToolVersion> {
        val result = mutableListOf<DownloadableToolVersion>()
        try {
            val rootElement = JsonParser.parseReader(json)
            val releases = rootElement.asJsonArray
            for (releaseElement in releases) {
                val release = releaseElement.asJsonObject
                val assets = release["assets"].asJsonArray
                for (assetElement in assets) {
                    val asset = assetElement.asJsonObject
                    val name = asset["name"].asString
                    val url = asset["browser_download_url"].asString
                    val toolVersion = NUnitToolVersion(name, url)
                    if (toolVersion.isValid) {
                        result.add(toolVersion)
                    }
                }
            }
        } catch (e: Exception) {
            LOG.debug(e)
        }

        return result
    }

    companion object {
        private val LOG = Logger.getInstance(
            ToolsParserImpl::class.java.name
        )
    }
}