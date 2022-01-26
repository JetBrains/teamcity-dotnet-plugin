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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

/**`
 * Provides a list of available .NET SDK.
 */

class DotnetSdkAgentPropertiesProvider(
        private val _toolProvider: ToolProvider,
        private val _dotnetVersionProvider: DotnetVersionProvider,
        private val _sdksProvider: DotnetSdksProvider,
        private val _pathsService: PathsService,
        private val _versionEnumerator: VersionEnumerator)
    : AgentPropertiesProvider {

    override val desription = ".NET SDK"

    override val properties: Sequence<AgentProperty>
        get() = sequence {
            // Detect .NET CLI path
            val dotnetPath = File(_toolProvider.getPath(DotnetConstants.EXECUTABLE))
            yield(AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, dotnetPath.canonicalPath))

            // Detect .NET CLI version
            val sdkVersion = _dotnetVersionProvider.getVersion(Path(dotnetPath.path), Path(_pathsService.getPath(PathType.Work).path))
            yield(AgentProperty(ToolInstanceType.DotNetCLI, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, sdkVersion.toString()))

            // Detect .NET SDKs
            for ((version, sdk) in _versionEnumerator.enumerate(_sdksProvider.getSdks(dotnetPath))) {
                val paramName = "${DotnetConstants.CONFIG_PREFIX_CORE_SDK}$version${DotnetConstants.CONFIG_SUFFIX_PATH}"
                yield(AgentProperty(ToolInstanceType.DotNetSDK, paramName, sdk.path.absolutePath))
            }
        }

    companion object {
        private val LOG = Logger.getLogger(DotnetSdkAgentPropertiesProvider::class.java)
    }
}