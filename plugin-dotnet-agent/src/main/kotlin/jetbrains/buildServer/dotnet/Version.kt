package jetbrains.buildServer.dotnet

import jetbrains.buildServer.util.IntegerOption
import jetbrains.buildServer.util.VersionComparatorUtil
import java.util.*
import kotlin.coroutines.experimental.buildSequence
import kotlin.text.*

class Version(
        vararg version: Int): Comparable<Version> {

    private var _zeroCounter = 0;

    val version = buildSequence {
        // Trim zero(s) at the end
        for (versionItem in version) {
            if (versionItem != 0) {
                yieldAll(repeat(0, _zeroCounter))
                _zeroCounter = 0;
                yield(versionItem)
            }
            else {
                _zeroCounter++;
            }
        }
    }.toList().toIntArray()

    override fun toString(): String = version.asSequence().plus(repeat(0, _zeroCounter)).joinToString(".")

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
            var version = text.splitToSequence('.').map {
                it.trim().toIntOrNull()
            }.toList()

            if( version.filter { it == null }.any()) {
                return Empty
            }

            return Version(*version.map { it as Int }.toList().toIntArray())
        }

        private fun <T>repeat(value: T, count: Int): Sequence<T> {
            var counter = count;
            return generateSequence { (counter--).takeIf { it > 0 } }.map { value }
        }
    }
}