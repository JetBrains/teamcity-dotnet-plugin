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

package jetbrains.buildServer.dotnet.discovery.dotnetFramework

import WindowsRegistryValueType
import jetbrains.buildServer.agent.*
import java.io.File

class DotnetPolicyRegistryVisitor(
        private val _environment: DotnetFrameworksEnvironment
)
    : DotnetFrameworksWindowsRegistryVisitor {
    private var _frameworks = mutableSetOf<DotnetFramework>()

    override val keys = Keys

    override fun getFrameworks()  = sequence {
        yieldAll(_frameworks)
        _frameworks.clear()
    }

    override fun visit(key: WindowsRegistryKey) = true

    override fun visit(value: WindowsRegistryValue): Boolean {
        if (value.key.parts.size == Deep + 2) {
            val subKey = value.key.parts.takeLast(2)
            if (
                    value.type == WindowsRegistryValueType.Str
                    && value.text.isNotBlank()
                    && subKey[0].startsWith("v")) {
                val majorVersion = Version.parse(subKey[0])
                if (majorVersion != Version.Empty && majorVersion.digits > 1) {
                    val subVersion = Version.parse(subKey[1])
                    if (subVersion != Version.Empty) {
                        val version = Version.parse("${majorVersion.major}${Version.Separator}${majorVersion.minor}${Version.Separator}${subVersion}")
                        _environment.tryGetRoot(value.key.bitness)?.let {
                            installRoot -> _frameworks.add(DotnetFramework(value.key.bitness.platform, version, File(installRoot, "v$version")))
                        }
                    }
                }
            }
        }

        return true
    }

    companion object {
        val Keys: Sequence<WindowsRegistryKey> get() =
            WindowsRegistryBitness
                    .values()
                    .asSequence()
                    .map {
                        WindowsRegistryKey.create(
                                it,
                                WindowsRegistryHive.LOCAL_MACHINE,
                                "SOFTWARE",
                                "Microsoft",
                                ".NETFramework",
                                "policy")
                    }

        private val Deep = Keys.first().parts.size
    }
}