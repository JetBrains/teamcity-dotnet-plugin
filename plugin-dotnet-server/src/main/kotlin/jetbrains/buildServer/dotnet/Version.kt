/*
 * Copyright 2000-2023 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.Range

infix fun Version.`in`(range: Range<Version>) = range.contains(this)

class Version(vararg val versions: Int, val release: String = "")
    : Comparable<Version> {

    private var _versions: IntArray

    init {
        _versions = versions.reversed().dropWhile { it == 0 }.reversed().toIntArray()
    }

    val size get() = _versions.size

    fun getPart(index: Int) = if(index < versions.size) versions[index] else 0

    fun trim() = Version(*_versions, release = release)

    override fun toString() =
            versions.joinToString(".") + release

    override fun compareTo(other: Version): Int {
        var compareVersions = versions
                .zip(other.versions) { it, second -> it.compareTo(second) }
                .filter { it != 0 }
                .firstOrNull()
                ?: versions.reversed().asSequence().dropWhile { it == 0 }.count().compareTo(other.versions.reversed().asSequence().dropWhile { it == 0 }.count())

        if(compareVersions !=0 ){
            return compareVersions
        }

        return release.compareTo(other.release)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Version

        if (release != other.release) return false
        if (!_versions.contentEquals(other._versions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = release.hashCode()
        result = 31 * result + _versions.contentHashCode()
        return result
    }


    companion object {
        private val VersionRegex = Regex("^([\\d\\.]+)([\\w\\d-\\.]*)$", RegexOption.IGNORE_CASE)

        fun tryParse(version: String): Version? {
            val match = VersionRegex.matchEntire(version)
            if(match == null)
            {
                return null
            }

            val versionParts = match.groupValues[1].split('.').map{ it.toIntOrNull() }
            if (versionParts.any() && versionParts.all { it != null }) {
                return Version(*versionParts.mapNotNull { it }.toIntArray(), release =  match.groupValues[2])
            }

            return null
        }
    }
}