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

package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger

class VisualStudioPackagesRegistryLocator(
        private val _windowsRegistry: WindowsRegistry)
    : VisualStudioPackagesLocator {

    override fun tryGetPackagesPath(): String? {
        var packagesPath: String? = null;
        for (key in RegKeys) {
            _windowsRegistry.accept(
                    key,
                    object : WindowsRegistryVisitor {
                        override fun visit(key: WindowsRegistryKey) = false
                        override fun visit(value: WindowsRegistryValue): Boolean {
                            if (value.type == WindowsRegistryValueType.Str && "CachePath".equals(value.key.parts.lastOrNull(), true)) {
                                packagesPath = value.text
                                LOG.debug("Using Visual Studio packages cache directory \"$packagesPath\"");
                                return false
                            }

                            return true
                        }
                    },
                    false)
        }

        return packagesPath
    }

    companion object {
        private val LOG = Logger.getLogger(VisualStudioPackagesRegistryLocator::class.java)

        internal val RegKeys =
                WindowsRegistryBitness
                        .values()
                        .asSequence()
                        .map {
                            WindowsRegistryKey.create(
                                    it,
                                    WindowsRegistryHive.LOCAL_MACHINE,
                                    "SOFTWARE",
                                    "Microsoft",
                                    "VisualStudio",
                                    "Setup")
                        }
    }
}