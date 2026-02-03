package jetbrains.buildServer.dotnet

// parses nuget package version from strings and file names
class SemanticVersionParserImpl : SemanticVersionParser {
    override fun tryParse(version: String) = VersionRegex.find(version)
        ?.destructured
        ?.let { (_, major, minor, patch, revision, _, preRelease, _) ->
            SemanticVersion(major.toInt(), minor.toInt(), patch.toInt(), if (revision.isNotEmpty()) revision.toInt() else 0, preRelease)
        }

    companion object {
        private val VersionRegex = Regex(
            """^([a-z\.-]+\.)?(\d+)\.(\d+)\.(\d+)(?:\.(\d+))?(-([\w\d-\.]+?))?((\.[A-Za-z][\w\d]*){1,2})?${'$'}""".trimMargin(),
            RegexOption.IGNORE_CASE
        )
    }
}