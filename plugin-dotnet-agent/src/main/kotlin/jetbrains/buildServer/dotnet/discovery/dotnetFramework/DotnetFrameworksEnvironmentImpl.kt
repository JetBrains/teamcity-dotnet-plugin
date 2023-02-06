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
import jetbrains.buildServer.agent.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.File

class DotnetFrameworksEnvironmentImpl(
        private val _windowsRegistry: WindowsRegistry)
    : DotnetFrameworksEnvironment {
    @Cacheable("tryGetRoot", key = "#bitness", sync = true)
    override fun tryGetRoot(bitness: WindowsRegistryBitness): File? {
        var root: File? = null
        _windowsRegistry.accept(
                WindowsRegistryKey.create(
                        bitness,
                        WindowsRegistryHive.LOCAL_MACHINE,
                        "SOFTWARE",
                        "Microsoft",
                        ".NETFramework"),

                object : WindowsRegistryVisitor {
                    override fun visit(key: WindowsRegistryKey) = true
                    override fun visit(value: WindowsRegistryValue): Boolean {
                        if (
                                value.type == WindowsRegistryValueType.Str
                                && value.text.isNotBlank()
                                && "InstallRoot".equals(value.key.parts.lastOrNull(), true)) {
                            root = File(value.text)
                            return false
                        }

                        return true
                    }
                },
                false
        )

        LOG.debug(".NET Framework ${bitness.platform.id} install root: \"${root ?: "empty"}\"");
        return root
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworksEnvironmentImpl::class.java)
    }
}