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
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File
import java.io.OutputStreamWriter

class ScriptResolverImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _pathsService: PathsService)
    : ScriptResolver {

    override fun resolve() =
            _parametersService
                    .tryGetParameter(ParameterType.Runner, ScriptConstants.SCRIPT_TYPE)
                    ?.let { ScriptType.tryParse(it) }
                    ?.let { getScriptFile(it) }
                    ?: throw RunBuildException("Cannot specify C# script.")

    private fun getScriptFile(scriptType: ScriptType) =
            when (scriptType) {
                ScriptType.File -> {
                    _parametersService.tryGetParameter(ParameterType.Runner, ScriptConstants.SCRIPT_FILE)?.let {
                        val scriptFile = File(it)
                        if (_fileSystemService.isAbsolute(scriptFile)) {
                            scriptFile
                        } else {
                            File(_pathsService.getPath(PathType.WorkingDirectory), scriptFile.path)
                        }
                    }
                }

                ScriptType.Custom -> {
                    _parametersService.tryGetParameter(ParameterType.Runner, ScriptConstants.SCRIPT_CONTENT)?.let { content ->
                        val tempPath = _pathsService.getPath(PathType.AgentTemp)
                        val scriptFile = _fileSystemService.generateTempFile(tempPath, "CSharpScript", ".csx")
                        _fileSystemService.write(scriptFile) {
                            OutputStreamWriter(it).use {
                                it.write(content)
                            }
                        }

                        scriptFile
                    } ?: throw RunBuildException("Cannot determine a script content.")
                }
            }
}