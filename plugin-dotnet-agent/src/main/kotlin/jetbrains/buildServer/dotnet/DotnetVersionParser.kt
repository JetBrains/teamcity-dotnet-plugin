package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.VersionParser
import java.util.regex.Pattern

class DotnetVersionParser : VersionParser {
    /**
     * Returns cleaned .net core sdk version.
     * **/
    override fun tryParse(output: Sequence<String>): String? =
            output
                .map { VersionPattern.matcher(it) }
                .filter { it.find() }
                .map { it.group(1).trim() }
                .firstOrNull()

    companion object {
        private val VersionPattern = Pattern.compile("^.*(\\d+\\.\\d+\\.\\d+[^\\s]*)")
    }
}