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
import jetbrains.buildServer.agent.ToolInstanceType
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ToolInstanceProvider
import jetbrains.buildServer.dotnet.DotnetConstants.CONFIG_PREFIX_DOTNET_VSTEST

class VisualStudioTestAgentPropertiesProvider(
        private val _visualStudioTestInstanceProvider: ToolInstanceProvider)
    : AgentPropertiesProvider {
    override val desription = "Visual Studio Test Console"

    override val properties: Sequence<AgentProperty> get() =
        _visualStudioTestInstanceProvider
                .getInstances()
                .asSequence()
                .filter { it.toolType == ToolInstanceType.VisualStudioTest }
                .map {
                    console ->
                    AgentProperty(ToolInstanceType.VisualStudioTest, "$CONFIG_PREFIX_DOTNET_VSTEST.${console.baseVersion.major}${Version.Separator}${console.baseVersion.minor}", console.installationPath.path)
                }
}