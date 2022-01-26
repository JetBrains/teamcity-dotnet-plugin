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

package jetbrains.buildServer.mono

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.MonoConstants
import java.io.File

class MonoToolEnvironment(
        private val _buildStepContext: BuildStepContext,
        private val _environment: Environment,
        private val _parametersService: ParametersService)
    : ToolEnvironment {

    private val _homePaths
        get() = when(_buildStepContext.isAvailable) {
            false -> _environment.tryGetVariable(MonoConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
            true -> _parametersService.tryGetParameter(ParameterType.Environment, MonoConstants.TOOL_HOME)?.let { sequenceOf(Path(it)) } ?: emptySequence()
        }

    override val homePaths: Sequence<Path>
        get() = extendByBin(_homePaths)

    override val defaultPaths: Sequence<Path>
        get() = emptySequence()

    override val environmentPaths: Sequence<Path>
        get() = extendByBin(_environment.paths)

    private fun extendByBin(paths: Sequence<Path>) = paths + paths.map { Path("${it.path}${File.separatorChar}bin") }
}