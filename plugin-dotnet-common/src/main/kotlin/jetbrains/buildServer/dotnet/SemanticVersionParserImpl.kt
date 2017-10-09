package jetbrains.buildServer.dotnet

class SemanticVersionParserImpl : SemanticVersionParser {
    override fun tryParse(version: String): SemanticVersion? {
        val matcher = VersionPattern.matcher(version)
        if(!matcher.find() || matcher.groupCount() < 5) {
            return null
        }

        return SemanticVersion(
                matcher.group("major").toInt(),
                matcher.group("minor").toInt(),
                matcher.group("build").toInt(),
                matcher.group("buildName") ?: "")
    }

    companion object {
        val VersionPattern = Regex("(^[a-z.]+\\.|^)(?<major>[\\d]+)\\.(?<minor>[\\d]+)\\.(?<build>[\\d]+)(-(?<buildName>\\w+)|)(.nupkg|)$", RegexOption.IGNORE_CASE).toPattern()
    }
}