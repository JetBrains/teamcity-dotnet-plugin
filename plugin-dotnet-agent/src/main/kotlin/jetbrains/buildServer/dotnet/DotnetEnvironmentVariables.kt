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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.common.MSBuildEnvironmentVariables.USE_SHARED_COMPILATION_ENV_VAR
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import jetbrains.buildServer.util.OSType

class DotnetEnvironmentVariables(
    private val _environment: Environment,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _additionalEnvironmentVariables: List<EnvironmentVariables>,
    private val _loggerResolver: LoggerResolver
) : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)

        yield(CommandLineEnvironmentVariable(MSBUILD_LOGGER_ENV_VAR, _loggerResolver.resolve(ToolType.MSBuild).canonicalPath))
        yield(CommandLineEnvironmentVariable(VSTEST_LOGGER_ENV_VAR, _loggerResolver.resolve(ToolType.VSTest).canonicalPath))
        val allowMessagesGuard = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD).toBoolean()
        if (allowMessagesGuard) {
            yield(CommandLineEnvironmentVariable(SERVICE_MESSAGES_PATH_ENV_VAR, _pathsService.getPath(PathType.AgentTemp).canonicalPath))
        }

        val useSharedCompilationParameter = _parametersService.tryGetParameter(ParameterType.Environment, USE_SHARED_COMPILATION_ENV_VAR)
        val useSharedCompilation = if (useSharedCompilationParameter?.equals("true", true) == true) "true" else "false"
        yield(CommandLineEnvironmentVariable(USE_SHARED_COMPILATION_ENV_VAR, useSharedCompilation))
        yieldAll(_additionalEnvironmentVariables.flatMap { it.getVariables(sdkVersion) })

        val home = if (_environment.os == OSType.WINDOWS) USER_PROFILE_ENV_VAR else HOME_ENV_VAR
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }
    }

    companion object {
        private const val USER_PROFILE_ENV_VAR = "USERPROFILE"
        private const val HOME_ENV_VAR = "HOME"
        internal const val MSBUILD_LOGGER_ENV_VAR = "TEAMCITY_MSBUILD_LOGGER"
        internal const val VSTEST_LOGGER_ENV_VAR = "TEAMCITY_VSTEST_LOGGER"
        internal const val SERVICE_MESSAGES_PATH_ENV_VAR = "TEAMCITY_SERVICE_MESSAGES_PATH"

        internal val defaultVariables = sequenceOf(
            CommandLineEnvironmentVariable("COMPlus_EnableDiagnostics", "0"),
            CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
            CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
            CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip")
        )
    }
}
