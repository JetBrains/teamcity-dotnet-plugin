/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.Logger
import java.io.File

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService,
        private val _toolEnvironment: ToolEnvironment,
        private val _dotnetSdksProviderImpl: DotnetSdksProvider)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
        executablePath
                ?.absolutePath
                ?: throw ToolCannotBeFoundException("""
                        Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                        .NET CLI toolchain directory or defined `${DotnetConstants.TOOL_HOME}` variable.""".trimIndent())

    private val executablePath: File? by lazy {
        var dotnetRuntime: File? = null
        val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, _toolEnvironment.homePaths + _toolEnvironment.defaultPaths + _toolEnvironment.environmentPaths)
        for (dotnetExecutable in executables) {
            if (_dotnetSdksProviderImpl.getSdks(dotnetExecutable).any()) {
                return@lazy dotnetExecutable
            }
            else {
                LOG.debug("Cannot find .NET Core SDK for <${dotnetExecutable}>.")
                dotnetRuntime = dotnetExecutable
            }
        }

        dotnetRuntime?.let {
            LOG.warn(".NET Core SDK was not found (found .NET Core runtime at path <${it.absolutePath}>). Install .NET Core SDK into the default location or set ${DotnetConstants.TOOL_HOME} environment variable pointing to the installation directory.")
        }

        return@lazy null
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.virtualContext.isVirtual) {
            DotnetConstants.EXECUTABLE
        } else {
            getPath(toolName)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetToolProvider::class.java)
    }
}