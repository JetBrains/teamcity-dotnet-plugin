package jetbrains.buildServer.dotnet

class NuGetPackageVersionParserImpl : NuGetPackageVersionParser {
    override fun tryParse(version: String): NuGetPackageVersion? {
        val matcher = _packageVersionPattern.matcher(version)
        if(!matcher.find() || matcher.groupCount() < 5) {
            return null
        }

        return NuGetPackageVersion(
                matcher.group("major").toInt(),
                matcher.group("minor").toInt(),
                matcher.group("build").toInt(),
                matcher.group("buildName") ?: "");
    }

    companion object {
        val _packageVersionPattern= Regex("(^[a-z.]+\\.|^)(?<major>[\\d]+)\\.(?<minor>[\\d]+)\\.(?<build>[\\d]+)(-(?<buildName>\\w+)|)(.nupkg|)$", RegexOption.IGNORE_CASE).toPattern()
    }
}