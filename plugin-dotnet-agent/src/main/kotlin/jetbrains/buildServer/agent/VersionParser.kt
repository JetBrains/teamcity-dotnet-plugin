package jetbrains.buildServer.agent

interface VersionParser {
    fun tryParse(output: Sequence<String>): String?
}