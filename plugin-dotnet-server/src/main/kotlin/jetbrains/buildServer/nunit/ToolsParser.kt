package jetbrains.buildServer.nunit

import jetbrains.buildServer.tools.available.DownloadableToolVersion
import java.io.Reader

interface ToolsParser {
    fun parse(json: Reader): List<DownloadableToolVersion>
}
