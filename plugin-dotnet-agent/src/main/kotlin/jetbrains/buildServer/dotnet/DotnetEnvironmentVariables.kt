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

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import jetbrains.buildServer.util.OSType

class DotnetEnvironmentVariables(
        private val _environment: Environment,
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService,
        private val _nugetEnvironmentVariables: EnvironmentVariables,
        private val _loggerResolver: LoggerResolver
) : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)

        yield(CommandLineEnvironmentVariable(MSBuildLoggerEnvVar, _loggerResolver.resolve(ToolType.MSBuild).canonicalPath))
        yield(CommandLineEnvironmentVariable(VSTestLoggerEnvVar, _loggerResolver.resolve(ToolType.VSTest).canonicalPath))
        val allowMessagesGuard = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_MESSAGES_GUARD)
                ?.let { it.equals("true", true) }
                ?: true
        if (allowMessagesGuard) {
            yield(CommandLineEnvironmentVariable(ServiceMessagesPathEnvVar, _pathsService.getPath(PathType.AgentTemp).canonicalPath))
        }

        val useSharedCompilation = if(_parametersService.tryGetParameter(ParameterType.Environment, UseSharedCompilationEnvVarName)?.equals("true", true) ?: false) "true" else "false"
        yield(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, useSharedCompilation))
        yieldAll(_nugetEnvironmentVariables.getVariables(sdkVersion))

        val home = if (_environment.os == OSType.WINDOWS) UserProfileEnvVar else HomeEnvVar
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetEnvironmentVariables::class.java)

        private const val UserProfileEnvVar = "USERPROFILE"
        private const val HomeEnvVar = "HOME"
        internal const val UseSharedCompilationEnvVarName = "UseSharedCompilation"
        internal const val MSBuildLoggerEnvVar = "TEAMCITY_MSBUILD_LOGGER"
        internal const val VSTestLoggerEnvVar = "TEAMCITY_VSTEST_LOGGER"
        internal const val ServiceMessagesPathEnvVar = "TEAMCITY_SERVICE_MESSAGES_PATH"

        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("COMPlus_EnableDiagnostics", "0"),
                CommandLineEnvironmentVariable("DOTNET_CLI_TELEMETRY_OPTOUT", "true"),
                CommandLineEnvironmentVariable("DOTNET_SKIP_FIRST_TIME_EXPERIENCE", "true"),
                CommandLineEnvironmentVariable("NUGET_XMLDOC_MODE", "skip"))
    }
}