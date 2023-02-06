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

package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.agent.AgentPropertiesProvider
import jetbrains.buildServer.agent.AgentProperty
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FAMEWORK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFrameworksProvider

class DotnetFrameworkRegistryAgentPropertiesProvider(
        private val _dotnetFrameworksProvider: DotnetFrameworksProvider
)
    : AgentPropertiesProvider {
    override val desription = "Dotnet Framework in registry"

    override val properties: Sequence<AgentProperty> get() =
        _dotnetFrameworksProvider
                .getFrameworks()
                .groupBy { it.platform }
                .map { curFrameworks ->
                    val latestDotnet4Version = curFrameworks.value.map { it.version }.filter { it.major == 4 }.maxByOrNull { it }

                    // Report only the latest version of .NET Framework 4.x
                    return@map curFrameworks
                            .value
                            .asSequence()
                            .filter { it.version.major != 4 || it.version == latestDotnet4Version }
                            .map {
                                LOG.debug("Found .NET Framework ${it.version} ${it.platform.id} at \"${it.path}\".")
                                sequence {
                                    val majorVersion = "${it.version.major}${Version.Separator}${it.version.minor}"
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${majorVersion}_${it.platform.id}", it.version.toString()))
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${majorVersion}_${it.platform.id}_Path", it.path.path))
                                    yield(AgentProperty(ToolInstanceType.DotNetFramework, "$CONFIG_PREFIX_DOTNET_FAMEWORK${it.version}_${it.platform.id}_Path", it.path.path))
                                }
                            }
                            .flatMap { it }
                }
                .asSequence()
                .flatMap { it }

    companion object {
        private val LOG = Logger.getLogger(DotnetFrameworkRegistryAgentPropertiesProvider::class.java)
    }
}
