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

package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants.RUNNER_DESCRIPTION
import jetbrains.buildServer.dotnet.discovery.dotnetRuntime.DotnetRuntimesProvider
import jetbrains.buildServer.excluding
import java.io.File
import jetbrains.buildServer.including
import jetbrains.buildServer.to

class ToolVersionResolverImpl(
    private val _fileSystemService: FileSystemService,
    private val _runtimesProvider: DotnetRuntimesProvider,
    private val _virtualContext: VirtualContext)
    : ToolVersionResolver {
    override fun resolve(toolPath: File): CsiTool {
        var supportedRuntimes = getSupportedRuntimes(toolPath).toList()

        var tool: CsiTool?
        if(!_virtualContext.isVirtual) {
            var runtimes = _runtimesProvider.getRuntimes().map { Version(it.version.major, it.version.minor) }.distinct().toList()
            supportedRuntimes =
                    supportedRuntimes
                    .filter {
                        val minVersion = it.runtimeVersion
                        val runtimeRange = minVersion.including() to Version(minVersion.major + 1).excluding()
                        val result = runtimes.any {runtimeRange.contains(it)}
                        LOG.debug("Min version: $minVersion, Runtime range: ${runtimeRange}, Result: $result")
                        result
                    }

            tool  = supportedRuntimes.maxByOrNull { it.runtimeVersion }
        }
        else {
            // Should return .NET Runtime LTS version for docker 6.0.0, 8.0.0 etc. according to https://dotnet.microsoft.com/en-us/platform/support/policy
            // or any newest available version
            tool = supportedRuntimes.filter { it.runtimeVersion.major % 2 == 0 }.maxByOrNull { it.runtimeVersion } ?: supportedRuntimes.maxByOrNull { it.runtimeVersion }
        }

        return tool ?: throw RunBuildException("Cannot find a supported version of $RUNNER_DESCRIPTION.")
    }

    private fun getSupportedRuntimes(toolsPath: File) =
            _fileSystemService.list(toolsPath)
                    .filter { _fileSystemService.isDirectory(it) }
                    .map {
                        LOG.debug("Goes through $it")
                        val version = VersionRegex.matchEntire(it.name)?.let {
                            Version.parse(it.groupValues[1])
                        } ?: Version.Empty

                        LOG.debug("Version: $version")
                        CsiTool(it, version)
                    }
                    .filter { it.runtimeVersion != Version.Empty }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        private val VersionRegex = Regex("^\\w+?([\\d\\.]+)$", RegexOption.IGNORE_CASE)
    }
}