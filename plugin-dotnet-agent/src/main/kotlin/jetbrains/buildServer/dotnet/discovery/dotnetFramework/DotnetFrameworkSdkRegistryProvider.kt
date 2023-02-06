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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ToolInstance
import jetbrains.buildServer.agent.runner.ToolInstanceFactory
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.Platform
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetFrameworkSdkRegistryProvider(
        private val _windowsRegistry: WindowsRegistry,
        private val _sdkInstanceFactory: ToolInstanceFactory)
    : ToolInstanceProvider {
    @Cacheable("ListOfDotnetFrameworkSdkFromRegistry", sync = true)
    override fun getInstances(): Collection<ToolInstance> {
            val sdks = mutableListOf<ToolInstance>()
            _windowsRegistry.accept(
                    RegKey,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = false
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            val name = value.key.parts.last()
                            if (
                                    value.type == WindowsRegistryValueType.Str
                                    && value.text.isNotBlank()
                                    && name.startsWith(VersionPrefix, true)) {
                                val version = Version.parse(name.substring(VersionPrefix.length))
                                if (version != Version.Empty) {
                                    if (version.digits == 2) {
                                        _sdkInstanceFactory.tryCreate(File(value.text), version, Platform.x86)?.let {
                                            LOG.debug("Found $it");
                                            sdks.add(it)
                                        }
                                    }
                                }
                                else {
                                    LOG.debug("Cannot parse version from $name")
                                }
                            }
                            return true
                        }

                    },
                    false)

            return sdks
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworkSdkRegistryProvider::class.java)
        private val VersionPrefix = "sdkInstallRootv"

        internal val RegKey = WindowsRegistryKey.create(
                WindowsRegistryBitness.Bitness32,
                WindowsRegistryHive.LOCAL_MACHINE,
                "SOFTWARE",
                "Microsoft",
                ".NETFramework")
    }
}