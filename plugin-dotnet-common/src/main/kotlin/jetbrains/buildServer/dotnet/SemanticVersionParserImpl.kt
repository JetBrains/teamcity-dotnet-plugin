

package jetbrains.buildServer.dotnet

// parses semver from strings and file names
class SemanticVersionParserImpl : SemanticVersionParser {
    override fun tryParse(version: String) = VersionRegex.find(version)
        ?.destructured
        ?.let { (_, major, minor, build, _, patch, _) ->
            SemanticVersion(major.toInt(), minor.toInt(), build.toInt(), patch)
        }

    companion object {
        private val VersionRegex = Regex(
            """^([a-z\.-]+\.)?(\d+)\.(\d+)\.(\d+)(-([\w\d-\.]+?))?((\.[A-Za-z][\w\d]*){1,2})?${'$'}""",
            RegexOption.IGNORE_CASE
        )
    }
}