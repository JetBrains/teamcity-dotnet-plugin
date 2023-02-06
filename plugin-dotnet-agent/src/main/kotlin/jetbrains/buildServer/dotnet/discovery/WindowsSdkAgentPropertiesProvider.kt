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

class WindowsSdkAgentPropertiesProvider(
        private val _sdkInstanceProvider: ToolInstanceProvider)
    : AgentPropertiesProvider {
    override val desription = "Windows SDK"

    override val properties: Sequence<AgentProperty> get() =
        _sdkInstanceProvider
                .getInstances()
                .asSequence()
                .filter { it.toolType == ToolInstanceType.WindowsSDK }
                .flatMap {
                    sdk ->
                    sequence {
                        val version = "WindowsSDKv${sdk.baseVersion.major}${Version.Separator}${sdk.baseVersion.minor}${sdk.baseVersion.release ?: ""}"
                        yield(AgentProperty(ToolInstanceType.WindowsSDK, version, sdk.detailedVersion.toString()))
                        yield(AgentProperty(ToolInstanceType.WindowsSDK, "${version}_Path", sdk.installationPath.path))
                    }
                }
}