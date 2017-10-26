package jetbrains.buildServer.dotnet

class SemanticVersionParserImpl : SemanticVersionParser {

    override fun tryParse(version: String): SemanticVersion? {
        VERSION_PATTERN.find(version)?.let {
            val (_, major, minor, build, _, patch, _) = it.destructured
            return SemanticVersion(major.toInt(), minor.toInt(), build.toInt(), patch)
        }
        return null
    }

    companion object {
        val VERSION_PATTERN = Regex(
                """^([a-z.]+\.)?(\d+)\.(\d+)\.(\d+)(-([a-z]+))?(\.nupkg)?${'$'}""",
                RegexOption.IGNORE_CASE
        )
    }
}