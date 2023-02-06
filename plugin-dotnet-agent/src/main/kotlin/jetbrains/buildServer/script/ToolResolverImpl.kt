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
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class ToolResolverImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _versionResolver: ToolVersionResolver)
    : ToolResolver {
    override fun resolve(): CsiTool {
        var toolPath = _parametersService
                .tryGetParameter(ParameterType.Runner, ScriptConstants.CLT_PATH)
                ?.let { File(it, "tools") }
                ?: throw RunBuildException("C# script runner path was not defined.")

        var runtimeVersion = Version.Empty

        if(!_fileSystemService.isExists(toolPath)) {
            throw RunBuildException("$toolPath was not found.")
        }

        if(_fileSystemService.isDirectory(toolPath)) {
            val tool = _versionResolver.resolve(toolPath)
            runtimeVersion = tool.runtimeVersion
            LOG.debug("Base path: ${tool.path}")
            toolPath = File(File(tool.path, "any"), ToolExecutable)
        }

        if(!_fileSystemService.isFile(toolPath)) {
            throw RunBuildException("Cannot find $toolPath.")
        }

        return CsiTool(toolPath, runtimeVersion)
    }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        const val ToolExecutable = "dotnet-csi.dll"
    }
}