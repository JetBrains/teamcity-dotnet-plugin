

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.ToolVersionOutputParser
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class InspectionVersionParser : ToolVersionOutputParser {
    override fun parse(output: Collection<String>): Version {
        return output
            .map { tryParseServiceMessage(it) ?: it }
            .mapNotNull { VERSION_OUTPUT_LINE_REGEX.matchEntire(it) }
            .map {
                val versionStr = it.groupValues[1]
                Version.parseSimplified(versionStr.trim())
            }
            .firstOrNull { !it.isEmpty() } ?: Version.Empty
    }

    private fun tryParseServiceMessage(messageStr: String): String? {
        try {
            return ServiceMessage.parse(messageStr)?.attributes?.get("text")
        } catch (e: Exception) {
            LOG.warn("Failed to parse service message $messageStr", e)
        }

        return messageStr
    }

    companion object {
        private val LOG = Logger.getLogger(InspectionVersionParser::class.java)
        private val VERSION_OUTPUT_LINE_REGEX = Regex("^Version:[^\\S\\r\\n]+(.+)\$")
    }
}