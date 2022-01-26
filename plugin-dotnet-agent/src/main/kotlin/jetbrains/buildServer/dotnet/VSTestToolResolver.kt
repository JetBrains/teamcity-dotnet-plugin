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

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class VSTestToolResolver(
        private val _virtualContext: VirtualContext,
        private val _parametersService: ParametersService,
        private val _dotnetToolResolver: ToolResolver,
        private val _stateWorkflowComposer: ToolStateWorkflowComposer)
    : ToolResolver {
    override val platform: ToolPlatform
        get() = _currentTool?.platform ?: ToolPlatform.CrossPlatform

    override val toolStateWorkflowComposer: ToolStateWorkflowComposer
        get() = _currentTool?.let {
            when (it.platform) {
                ToolPlatform.CrossPlatform -> _dotnetToolResolver.toolStateWorkflowComposer
                else -> _stateWorkflowComposer
            }
        } ?: _dotnetToolResolver.toolStateWorkflowComposer

    override val executable: ToolPath
        get() {
            _currentTool?.let {
                when (it.platform) {
                    ToolPlatform.Windows -> {
                        val vstestTool = "teamcity.dotnet.vstest.${it.version}.0"
                        return ToolPath(tryGetTool(vstestTool) ?: throw RunBuildException(ToolCannotBeFoundException(vstestTool)))
                    }
                    else -> {
                    }
                }
            }

            return _dotnetToolResolver.executable
        }

    override val isCommandRequired: Boolean
        get() {
            _currentTool?.let {
                return it.platform == ToolPlatform.CrossPlatform
            }

            return true
        }

    private val _currentTool: Tool?
        get() {
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VSTEST_VERSION)?.let {
                return Tool.tryParse(it)
            }

            return null
        }

    private fun tryGetTool(parameterName: String): Path? {
        if (_virtualContext.isVirtual) {
            return Path("vstest.console.exe")
        }

        return _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            return Path(File(it).canonicalPath)
        }
    }
}