package jetbrains.buildServer.dotnet

import java.util.*

infix fun Version.`in`(range: Range<Version>) = range.contains(this)

class Version(vararg val versions: Int)
    : Comparable<Version> {

    private var _versions: IntArray

    init {
        _versions = versions.reversed().dropWhile { it == 0 }.reversed().toIntArray()
    }

    val size get() = _versions.size

    fun getPart(index: Int) = if(index < versions.size) versions[index] else 0

    fun trim() = Version(*_versions)

    override fun toString() =
            versions.joinToString(".")

    override fun compareTo(other: Version) =
            versions
                    .zip(other.versions) { it, second -> it.compareTo(second) }
                    .filter { it != 0 }
                    .firstOrNull()
                    ?: versions.reversed().asSequence().dropWhile { it == 0 }.count().compareTo(other.versions.reversed().asSequence().dropWhile { it == 0 }.count())

    override fun equals(other: Any?) =
        if(other is Version) _versions contentEquals other._versions else false

    override fun hashCode(): Int = Arrays.hashCode(_versions)

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