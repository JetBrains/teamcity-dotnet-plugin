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
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_DEFAULT_BITNESS
import jetbrains.buildServer.util.OSType
import java.io.File

class MSBuildToolResolver(
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
        get() =
            _currentTool?.let {
                when (it.platform) {
                    ToolPlatform.Windows -> {
                        val x86Tool = "MSBuildTools${it.version}.0_x86_Path"
                        val x64Tool = "MSBuildTools${it.version}.0_x64_Path"
                        when (it.bitness) {
                            ToolBitness.X64 -> {
                                return ToolPath(tryGetWindowsTool(x64Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x64Tool)))
                            }
                            ToolBitness.X86 -> {
                                return ToolPath(tryGetWindowsTool(x86Tool) ?: throw RunBuildException(ToolCannotBeFoundException(x86Tool)))
                            }
                            else -> {
                                val defaultBitness = _parametersService.tryGetParameter(ParameterType.Configuration, PARAM_DEFAULT_BITNESS)?.let {
                                    ToolBitness.tryParse(it)
                                } ?: ToolBitness.X86

                                when(defaultBitness) {
                                    ToolBitness.X64 -> getDefaultToolPath(x64Tool, x86Tool)
                                    else -> getDefaultToolPath(x86Tool, x64Tool)
                                }
                            }
                        }
                    }
                    ToolPlatform.Mono -> {
                        val monoTool = MonoConstants.CONFIG_PATH
                        return ToolPath(tryGetMonoTool(monoTool) ?: throw RunBuildException(ToolCannotBeFoundException(monoTool)))
                    }
                    else -> {
                        return _dotnetToolResolver.executable
                    }
                }
            } ?: _dotnetToolResolver.executable

    private fun getDefaultToolPath(majorTool: String, minorTool: String): ToolPath {
        tryGetWindowsTool(majorTool)?.let {
            return ToolPath(it)
        }

        return ToolPath(tryGetWindowsTool(minorTool) ?: throw RunBuildException(ToolCannotBeFoundException(majorTool)))
    }

    override val isCommandRequired: Boolean
        get() =
            _currentTool?.let {
                return it.platform == ToolPlatform.CrossPlatform
            } ?: true

    private val _currentTool: Tool?
        get() =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_MSBUILD_VERSION)?.let {
                return Tool.tryParse(it)
            }

    private fun tryGetWindowsTool(parameterName: String): Path? {
        val executable =  MSBuildWindowsTooName
        if (_virtualContext.isVirtual) {
            return Path(executable)
        }

        return _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            return Path(File(it, executable).canonicalPath)
        }
    }

    private fun tryGetMonoTool(parameterName: String): Path? {
        val executable =  when (_virtualContext.targetOSType) {
            OSType.WINDOWS -> MSBuildMonoWindowsToolName
            else -> MSBuildMonoToolName
        }

        if (_virtualContext.isVirtual) {
            return Path(executable)
        }

        return _parametersService.tryGetParameter(ParameterType.Configuration, parameterName)?.let {
            val baseDirectory = File(it).canonicalFile.parent
            return Path(File(baseDirectory, executable).canonicalPath)
        }
    }

    companion object {
        const val MSBuildWindowsTooName = "MSBuild.exe"
        const val MSBuildMonoWindowsToolName = "msbuild.bat"
        const val MSBuildMonoToolName = "msbuild"
    }
}