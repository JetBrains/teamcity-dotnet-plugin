

package jetbrains.buildServer.inspect

import jetbrains.buildServer.XmlElement
import jetbrains.buildServer.agent.runner.LoggerService

/**
 * Provides download specification, for example:
 * <Download Id="Plugin.Id" Version="1.2.0.0"></Download>
 */
class DownloadPluginXmlElementGenerator(
    private val _loggerService: LoggerService
) : PluginXmlElementGenerator {
    override val sourceId = "download"

    override fun generateXmlElement(strValue: String) =
        strValue.split("/").let { parts ->
            val result = XmlElement("Download")
            if (parts.size == 2) {
                result
                    .withAttribute("Id", parts[0])
                    .withAttribute("Version", parts[1])
            } else {
                _loggerService.writeWarning("Invalid R# CLT plugin descriptor for downloading: \"$strValue\", it will be ignored.")
            }

            result
        }
}