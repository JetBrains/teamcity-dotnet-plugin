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

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class DotnetToolEnvironment(
        private val _buildStepContext: BuildStepContext,
        private val _environment: Environment,
        private val _parametersService: ParametersService)
    : ToolEnvironment {

    override val homePaths: Sequence<Path>
        get() = when(_buildStepContext.isAvailable) {
            false -> _environment.tryGetVariable(DotnetConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
            true -> _parametersService.tryGetParameter(ParameterType.Environment, DotnetConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
        }

    override val defaultPaths: Sequence<Path>
        get() = sequenceOf(
                when(_environment.os) {
                    OSType.WINDOWS -> _environment.tryGetVariable(DotnetConstants.PROGRAM_FILES_ENV_VAR)
                            ?.let {
                                Path("$it\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
                            }
                            ?: Path("C:\\Program Files\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
                    OSType.UNIX -> Path("/usr/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
                    OSType.MAC -> Path("/usr/local/share/${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
                }
        )

    override val environmentPaths: Sequence<Path>
        get() = _environment.paths
}