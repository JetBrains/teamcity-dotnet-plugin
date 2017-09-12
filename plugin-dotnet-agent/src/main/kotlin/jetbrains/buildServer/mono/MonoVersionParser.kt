package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.VersionParser
import java.util.regex.Pattern

class MonoVersionParser : VersionParser {
    /**
     * Returns cleaned mono version.
     * **/
    override fun tryParse(output: Sequence<String>): String? =
            output
                .map { VersionPattern.matcher(it) }
                .filter { it.find() }
                .map { it.group(1).trim() }
                .firstOrNull()

    companion object {
        private val VersionPattern = Pattern.compile("^.*\\sversion\\s(\\d+\\.\\d+\\.\\d+).*")
    }
}