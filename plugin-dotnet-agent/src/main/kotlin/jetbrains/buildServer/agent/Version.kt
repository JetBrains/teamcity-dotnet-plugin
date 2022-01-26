/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

package jetbrains.buildServer.agent

class Version private constructor(
        val major: Int,
        val minor: Int,
        val digits: Int,
        val patch: Int,
        val build: Int = 0,
        val minorBuild: Int = 0,
        val release: String? = null,
        private val metadata: String? = null,
        private val text: String? = null)
    : Comparable<Version> {

    constructor(major: Int) : this(major, 0, 1, 0, 0, 0)

    constructor(major: Int, minor: Int) : this(major, minor, 2, 0, 0, 0)

    constructor(major: Int, minor: Int, patch: Int) : this(major, minor, 3, patch, 0, 0)

    constructor(major: Int, minor: Int, patch: Int, release: String) : this(major, minor,4, patch, 0, 0, release)

    constructor(major: Int, minor: Int, patch: Int, build: Int) : this(major, minor,4, patch, build, 0, null)

    constructor(major: Int, minor: Int, patch: Int, build: Int, minorBuild: Int) : this(major, minor,5, patch, build, minorBuild, null)

    private val versionString: String = buildString {
        append(major)
        append(Separator)
        append(minor)
        append(Separator)
        append(patch)
        if (build > 0) append('.').append(build)
        if (minorBuild > 0) append('.').append(minorBuild)
        if (release != null) append('-').append(release)
        if (metadata != null) append('+').append(metadata)
    }

    override fun toString(): String = text ?: versionString

    override fun compareTo(other: Version): Int {
        major.compareTo(other.major).run { if (this != 0) return this }
        minor.compareTo(other.minor).run { if (this != 0) return this }
        patch.compareTo(other.patch).run { if (this != 0) return this }
        build.compareTo(other.build).run { if (this != 0) return this }
        minorBuild.compareTo(other.minorBuild).run { if (this != 0) return this }
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
        const val Separator = '.'
        private val VERSION_PATTERN = Regex("^[^\\d^\\.]*([0-9]+)(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+(([0-9A-Za-z-\\.]+)))?[^\\d^\\.]*$", RegexOption.IGNORE_CASE)
        val Empty: Version = Version(0, 0, 0, 0, 0, 0, null, null, null)

        fun parse(text: String): Version {
            VERSION_PATTERN.matchEntire(text)?.let {

                val (majorStr, minorStr, patchStr, buildStr, minorBuildStr, releaseStr, metadataStr) = it.destructured
                var digits = 0
                var newText: String = ""

                val major = majorStr.toIntOrNull()?.let {
                    newText += majorStr
                    digits++
                    it
                } ?: 0

                val minor = minorStr.toIntOrNull()?.let {
                    newText += Separator
                    newText += minorStr
                    digits++
                    it
                } ?: 0

                val patch = patchStr.toIntOrNull()?.let {
                    newText += Separator
                    newText += patchStr
                    digits++
                    it
                } ?: 0

                val build = buildStr.toIntOrNull()?.let {
                    newText += Separator
                    newText += buildStr
                    digits++
                    it
                } ?: 0

                val minorBuild = minorBuildStr.toIntOrNull()?.let {
                    newText += Separator
                    newText += minorBuildStr
                    digits++
                    it
                } ?: 0

                var release: String? = null
                if (!releaseStr.isEmpty()) {
                    newText += "-"
                    newText += releaseStr
                    release = releaseStr
                }

                var metadata: String? = null
                if (!metadataStr.isEmpty()) {
                    newText += "+"
                    newText += metadataStr
                    metadata = metadataStr
                }

                return Version(major, minor, digits, patch, build, minorBuild, release, metadata, newText)
            }

            return Empty
        }

        val LastVersionWithoutSharedCompilation: Version = Version(2, 1, 105)
        val MultiAdapterPathVersion: Version = Version(2, 1, 102)
        val MultiAdapterPath_5_0_103_Version: Version = Version(5, 0, 103)
        val NoMultiAdapterPathVersion: Version = Version(5, 0, 103)
        val CredentialProviderVersion: Version = Version(2, 1, 400)
    }
}