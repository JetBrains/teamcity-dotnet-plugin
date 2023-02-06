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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.agent.Logger

class PackagesProviderImpl(
        sources: List<PluginSource>)
    : PackagesProvider {

    private val _sources: Map<String, PluginSource> = sources.associate { it.id.lowercase() to it }

    override fun getPackages(specifications: String) =
        E("Packages",
                specifications
                        .lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .mapNotNull { specification ->
                            val match = PluginSourceRegex.matchEntire(specification)
                            if (match != null) {
                                val sourceId = match.groupValues[1]
                                val source = _sources[sourceId.lowercase()]
                                if (source != null) {
                                    source.getPlugin(match.groupValues[2])
                                } else {
                                    LOG.info("Unrecognized plugin source: $sourceId")
                                    null
                                }
                            } else {
                                LOG.info("Invalid R# plugin specification: $specification")
                                null
                            }
                        }
                        .asSequence()
        )

    companion object {
        private val LOG = Logger.getLogger(PackagesProviderImpl::class.java)
        private val PluginSourceRegex = Regex("([^\\s]+)\\s+(.*)")
    }
}
