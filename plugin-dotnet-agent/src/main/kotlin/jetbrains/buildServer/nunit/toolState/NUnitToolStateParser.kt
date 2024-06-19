package jetbrains.buildServer.nunit.toolState

import java.util.regex.Pattern

class NUnitToolStateParser {
    fun parse(exitCode: Int, stdOut: List<String>): NUnitToolState {
        var nUnitVersion = ""
        val extensions = ArrayList<String>()
        for (line in stdOut) {
            if ("" == nUnitVersion) {
                val versionMatcher = nUnitVersionPattern.matcher(line)
                if (versionMatcher.find() && versionMatcher.groupCount() == 1) {
                    nUnitVersion = versionMatcher.group(1)
                }
            }

            if (exitCode == 0) {
                val extensionMatcher = extensionPattern.matcher(line)
                if (extensionMatcher.find() && extensionMatcher.groupCount() == 1) {
                    val extension = extensionMatcher.group(1)
                    if (extension != null) {
                        extensions.add(extension)
                    }
                }
            }
        }

        return NUnitToolState(nUnitVersion, extensions)
    }

    companion object {
        private val nUnitVersionPattern: Pattern = Pattern.compile(
            "NUnit Console(?: Runner)? (\\d+\\.\\d+(?:\\.\\d+)?)",
            Pattern.CASE_INSENSITIVE or Pattern.MULTILINE
        )
        private val extensionPattern: Pattern =
            Pattern.compile("^\\s*Extension:\\s([\\w\\.\\d]+)\\s*.*$", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE)
    }
}