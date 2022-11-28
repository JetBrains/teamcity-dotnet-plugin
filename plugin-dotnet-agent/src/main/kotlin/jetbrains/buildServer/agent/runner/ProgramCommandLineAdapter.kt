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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType

class ProgramCommandLineAdapter(
    private val _argumentsService: ArgumentsService,
    private val _environment: Environment,
    private val _buildStepContext: BuildStepContext,
    private val _virtualContext: VirtualContext,
    private val _parametersService: ParametersService,
) : ProgramCommandLine, ProgramCommandLineFactory {

    private var _commandLine: CommandLine = CommandLine(null, TargetType.NotApplicable, Path(""), Path(""))

    override fun getExecutablePath(): String = _commandLine.executableFile.path

    override fun getWorkingDirectory(): String = _commandLine.workingDirectory.path

    override fun getArguments(): MutableList<String> = _commandLine.arguments.map {
        if(_environment.os == OSType.WINDOWS) _argumentsService.normalize(it.value) else it.value
    }.toMutableList()

    override fun getEnvironment(): MutableMap<String, String> {
        val environmentVariables = _buildStepContext.runnerContext.buildParameters.environmentVariables.toMutableMap()
        _commandLine.environmentVariables.forEach {
            // add plugin specific env vars in addition to build and user defined env vars;
            // or override if it's allowed for some variables
            if (!environmentVariables.containsKey(it.name) || isEnvVarOverrideAllowed(it.name)) {
                environmentVariables[it.name] = it.value
            }
        }

        if (_virtualContext.isVirtual && _commandLine.chain.any { it.target == TargetType.SystemDiagnostics }) {
            // Hides docker build log messages
            environmentVariables[ENV_DOCKER_QUIET_MODE] = "true";
        }

        return environmentVariables
    }

    private fun isEnvVarOverrideAllowed(variableName: String) =
        OverridableTempDirectoryEnvVars.contains(variableName) && _isTempDirOverrideAllowed

    private val _isTempDirOverrideAllowed: Boolean get() = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_DOTCOVER_TEMP_DIR_OVERRIDE)
        ?.let { !it.trim().equals("false", true) }
        ?: true

    override fun create(commandLine: CommandLine): ProgramCommandLine {
        this._commandLine = commandLine;
        return this;
    }

    companion object {
        internal const val ENV_DOCKER_QUIET_MODE = "TEAMCITY_DOCKER_QUIET_MODE"
        internal val OverridableTempDirectoryEnvVars = setOf("TEMP", "TMP", "TMPDIR")
    }
}