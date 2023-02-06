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
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.discovery.dotnetFramework.DotnetFrameworksProvider

class TargetingPackAgentPropertiesProvider(
        private val _frameworksProvider: DotnetFrameworksProvider
)
    : AgentPropertiesProvider {

    override val desription = "Dotnet Framework targeting pack"

    override val properties: Sequence<AgentProperty> get() =
        _frameworksProvider
                .getFrameworks()
                .filter { it.version.major == 2 && it.version.minor == 0 }
                .distinctBy { Version(it.version.major, it.version.minor) }
                .map {
                    framework ->
                    LOG.debug("Found .NET Framework targeting pack ${framework.version.toString()} at \"${framework.path.path}\".")
                    AgentProperty(ToolInstanceType.TargetingPack, "$CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK${framework.version.major}${Version.Separator}${framework.version.minor}_Path", framework.path.path)
                }

    companion object {
        private val LOG = Logger.getLogger(TargetingPackAgentPropertiesProvider::class.java)
    }
}