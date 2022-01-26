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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.impl.config.BuildAgentConfigurablePaths
import jetbrains.buildServer.agent.plugins.beans.PluginDescriptor
import java.io.File
import java.util.*

class PathsServiceImpl(
        private val _buildStepContext: BuildStepContext,
        private val _buildAgentConfiguration: BuildAgentConfiguration,
        private val _buildAgentConfigurablePaths: BuildAgentConfigurablePaths,
        private val _pluginDescriptor: PluginDescriptor,
        private val _fileSystemService: FileSystemService) : PathsService {
    override val uniqueName: String
        get() = UUID.randomUUID().toString().replace("-", "")

    override fun uniqueName(basePath: File, extension: String): File {
        for (num in 1..Int.MAX_VALUE) {
            val file = File(basePath, "$num$extension")
            if (!_fileSystemService.isExists(file)) {
                return file
            }
        }

        throw RunBuildException("Cannot generate unique name in $basePath for $extension.");
    }

    override fun getPath(pathType: PathType): File = when (pathType) {
        PathType.WorkingDirectory -> _buildStepContext.runnerContext.workingDirectory.canonicalFile
        PathType.Checkout -> _buildStepContext.runnerContext.build.checkoutDirectory.canonicalFile
        PathType.AgentTemp -> _buildAgentConfigurablePaths.agentTempDirectory.canonicalFile
        PathType.BuildTemp -> _buildAgentConfigurablePaths.buildTempDirectory.canonicalFile
        PathType.GlobalTemp -> _buildAgentConfigurablePaths.cacheDirectory.canonicalFile
        PathType.Plugins -> _buildAgentConfiguration.agentPluginsDirectory.canonicalFile
        PathType.Plugin -> _pluginDescriptor.pluginRoot.canonicalFile
        PathType.Tools -> _buildAgentConfiguration.agentToolsDirectory.canonicalFile
        PathType.Lib -> _buildAgentConfiguration.agentLibDirectory.canonicalFile
        PathType.Work -> _buildAgentConfiguration.workDirectory.canonicalFile
        PathType.System -> _buildAgentConfiguration.systemDirectory.canonicalFile
        PathType.Bin -> File(_buildAgentConfiguration.agentHomeDirectory, "bin").canonicalFile
        PathType.Config -> _buildAgentConfigurablePaths.agentConfDirectory.canonicalFile
        PathType.Log -> _buildAgentConfigurablePaths.agentLogsDirectory.canonicalFile
        PathType.GlobalCache -> _buildAgentConfigurablePaths.cacheDirectory
        PathType.Cache -> _buildAgentConfigurablePaths.getCacheDirectory(_buildStepContext.runnerContext.runType)
        PathType.CachePerCheckout -> File(getPath(PathType.Cache), getPath(PathType.Checkout).name)
    }

    override fun getPath(pathType: PathType, runnerType: String): File =
            when(pathType) {
                PathType.Cache -> _buildAgentConfigurablePaths.getCacheDirectory(runnerType)
                PathType.CachePerCheckout -> File(getPath(PathType.Cache, runnerType), getPath(PathType.Checkout).name)
                else -> getPath(pathType)
            }

    override fun getTempFileName(extension: String): File =
        uniqueName(getPath(PathType.AgentTemp), extension)
}