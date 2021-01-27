package jetbrains.buildServer.dotnet

infix fun Version.`in`(range: Range<Version>) = range.contains(this)

class Version(vararg val versions: Int)
    : Comparable<Version> {

    override fun toString() =
            versions.joinToString(".")

    override fun compareTo(other: Version) =
            versions
                    .zip(other.versions) { it, other -> it.compareTo(other) }
                    .filter { it != 0 }
                    .firstOrNull()
                    ?: versions.reversed().asSequence().dropWhile { it == 0 }.count().compareTo(other.versions.reversed().asSequence().dropWhile { it == 0 }.count())

    override fun equals(other: Any?) =
        if(other is Version) versions contentEquals other.versions else false

    override fun hashCode(): Int {
        return super.hashCode()
    }

    companion object {
        private val VersionRegex = Regex("^[\\d\\.]+$", RegexOption.IGNORE_CASE)

        fun tryParse(version: String): Version? {
            val versionParts = if (VersionRegex.matches(version)) version.split('.').map{ it.toIntOrNull() } else null
            if (versionParts != null && versionParts.any() && versionParts.all { it != null }) {
                return Version(*versionParts.mapNotNull { it }.toIntArray())
            }

            return null
        }
    }
}