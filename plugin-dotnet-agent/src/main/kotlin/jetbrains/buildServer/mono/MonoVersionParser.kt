

package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.ToolVersionOutputParser
import jetbrains.buildServer.agent.Version
import java.util.regex.Pattern

class MonoVersionParser : ToolVersionOutputParser {
    /**
     * Returns cleaned mono version.
     * **/
    override fun parse(output: Collection<String>): Version =
        output
            .map { VersionPattern.matcher(it) }
            .filter { it.find() }
            .map { it.group(1).trim() }
            .firstOrNull()?.let { Version.parse(it) } ?: Version.Empty

    companion object {
        private val VersionPattern = Pattern.compile("^.*\\sversion\\s(\\d+\\.\\d+\\.\\d+).*")
    }
}