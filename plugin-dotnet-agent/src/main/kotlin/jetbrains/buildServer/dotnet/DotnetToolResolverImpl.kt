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
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.agent.Logger

class DotnetToolResolverImpl(
        private val _parametersService: ParametersService,
        private val _toolEnvironment: ToolEnvironment,
        private val _toolSearchService: ToolSearchService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext,
        override val toolStateWorkflowComposer: ToolStateWorkflowComposer)
    : DotnetToolResolver {
    override val platform: ToolPlatform
        get() = ToolPlatform.CrossPlatform

    override val executable: ToolPath
        get() {
            try {
                val homePaths = _toolEnvironment.homePaths.toList()
                val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, _toolEnvironment.homePaths).map { Path(it.path) } + tryFinding(ParameterType.Configuration, DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH)
                var dotnetPath = executables.firstOrNull()
                if (dotnetPath == null) {
                    if(_virtualContext.isVirtual) {
                        dotnetPath = getHomePath(_environment.os, homePaths)
                    }
                    else {
                        throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
                    }
                }

                val virtualPath = when {
                    _virtualContext.isVirtual -> getHomePath(_virtualContext.targetOSType, homePaths)
                    else -> dotnetPath
                }

                return ToolPath(dotnetPath, virtualPath, homePaths)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true

    private fun getHomePath(os: OSType, homePaths: List<Path>) = when {
        homePaths.isNotEmpty() -> Path("${homePaths[0].path}${separator(os)}${defaultExecutable(os).path}")
        else -> defaultExecutable(os)
    }

    private fun separator(os: OSType) = when {
        os == OSType.WINDOWS -> '\\'
        else -> '/'
    }

    private fun defaultExecutable(os: OSType) = when {
        os == OSType.WINDOWS -> Path("dotnet.exe")
        else -> Path("dotnet")
    }

    private fun tryFinding(parameterType: ParameterType, parameterName: String): Sequence<Path> {
        val dotnetPath = _parametersService.tryGetParameter(parameterType, parameterName)
        LOG.debug("$parameterType variable \"$parameterName\" is \"$dotnetPath\"")
        return if (!dotnetPath.isNullOrBlank()) sequenceOf(Path(dotnetPath)) else emptySequence<Path>()
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetToolResolverImpl::class.java)
    }
}