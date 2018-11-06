@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import java.util.*

class Version(vararg version: Int) : Comparable<Version> {
    val version = sequence {
        // Trim zero(s) at the end
        var zeroCounter = 0
        for (versionItem in version) {
            if (versionItem != 0) {
                yieldAll(repeat(0, zeroCounter))
                zeroCounter = 0
                yield(versionItem)
            } else {
                zeroCounter++
            }
        }
    }.toList().toIntArray()

    val fullVersion = version

    override fun toString(): String = fullVersion.joinToString(".")

    override fun compareTo(other: Version): Int =
            version
                    .zip(other.version)
                    .filter { it.first != it.second }
                    .map { it.first - it.second }
                    .lastOrNull() ?: 0
                    .let { if (it != 0) it else version.size - other.version.size }

    override fun hashCode(): Int = Arrays.hashCode(version)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Version
        return Arrays.equals(version, other.version)
    }

    companion object {
        val Empty: Version = Version()

        fun parse(text: String): Version {
            val version = text.splitToSequence('.').map {
                it.trim().toIntOrNull()
            }.toList()

            if (version.filter { it == null }.any()) {
                return Empty
            }

            return Version(*version.map { it as Int }.toList().toIntArray())
        }

        private fun <T> repeat(value: T, count: Int): Sequence<T> {
            var counter = count
            return generateSequence { (counter--).takeIf { it > 0 } }.map { value }
        }

        val LastVersionWithoutSharedCompilation: Version = Version(2, 1, 105)
        val MultiAdapterPathVersion: Version = Version(2, 1, 102)
    }
}