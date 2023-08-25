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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariables
import java.io.OutputStreamWriter

class CommandLineFactoryImpl(
        private val _pathsService: PathsService,
        private val _toolResolver: ToolResolver,
        private val _environmentVariables: List<EnvironmentVariables>,
        private val _fileSystemService: FileSystemService,
        private val _rspContentFactory: RspContentFactory,
        private val _virtualContext: VirtualContext)
    : CommandLineFactory {

    override fun create(): CommandLine {
        val rspFile = _fileSystemService.generateTempFile(_pathsService.getPath(PathType.AgentTemp), "options", ".rsp")
        _fileSystemService.write(rspFile) {
            OutputStreamWriter(it).use {
                for (line in _rspContentFactory.create()) {
                    it.write(line)
                    it.write("\n")
                }
            }
        }

        val csiTool = _toolResolver.resolve()
        return CommandLine(
            null,
            TargetType.Tool,
            Path(""),
            Path(_pathsService.getPath(PathType.WorkingDirectory).path),
            listOf(
                CommandLineArgument(_virtualContext.resolvePath(csiTool.path.path)),
                CommandLineArgument("@${_virtualContext.resolvePath(rspFile.path)}")
            ),
            _environmentVariables.flatMap { it.getVariables(csiTool.runtimeVersion) }
        )
    }
}
