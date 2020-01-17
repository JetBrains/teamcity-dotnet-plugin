/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

class Version private constructor(val major: Int,
                                  val minor: Int,
                                  private val patch: Int,
                                  private val build: Int = 0,
                                  private val release: String? = null,
                                  private val metadata: String? = null) : Comparable<Version> {

    constructor(major: Int) : this(major, 0)

    constructor(major: Int, minor: Int) : this(major, minor, 0)

    constructor(major: Int, minor: Int, patch: Int) : this(major, minor, patch, 0)

    private val versionString: String = buildString {
        append(major)
        append('.')
        append(minor)
        append('.')
        append(patch)
        if (build > 0) append('.').append(build)
        if (release != null) append('-').append(release)
        if (metadata != null) append('+').append(metadata)
    }

    override fun toString(): String = versionString

    override fun compareTo(other: Version): Int {
        major.compareTo(other.major).run { if (this != 0) return this }
        minor.compareTo(other.minor).run { if (this != 0) return this }
        patch.compareTo(other.patch).run { if (this != 0) return this }
        build.compareTo(other.build).run { if (this != 0) return this }
        return if (release == null) {
            if (other.release == null) 0 else 1
        } else {
            if (other.release == null) -1 else release.compareTo(other.release)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Version) return false
        return compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return versionString.hashCode()
    }

    companion object {
        private val VERSION_PATTERN = Regex("^([0-9]+)(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+(([0-9A-Za-z-\\.]+)))?$", RegexOption.IGNORE_CASE)
        val Empty: Version = Version(0, 0, 0, 0)

        fun parse(text: String): Version {
            VERSION_PATTERN.matchEntire(text)?.let {
                val (majorStr, minorStr, patchStr, buildStr, releaseStr, metadataStr) = it.destructured
                val major = majorStr.toIntOrNull() ?: 0
                val minor = minorStr.toIntOrNull() ?: 0
                val patch = patchStr.toIntOrNull() ?: 0
                val build = buildStr.toIntOrNull() ?: 0
                val release = if (releaseStr.isEmpty()) null else releaseStr
                val metadata = if (metadataStr.isEmpty()) null else metadataStr
                return Version(major, minor, patch, build, release, metadata)
            }

            return Empty
        }

        val LastVersionWithoutSharedCompilation: Version = Version(2, 1, 105)
        val MultiAdapterPathVersion: Version = Version(2, 1, 102)
    }
}