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

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.OSType
import jetbrains.buildServer.agent.Logger
import java.io.File

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _nugetEnvironmentVariables: EnvironmentVariables,
        private val _virtualContext: VirtualContext,
        private val _loggerResolver: LoggerResolver)
    : EnvironmentVariables {
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

        val useSharedCompilation = if(_parametersService.tryGetParameter(ParameterType.Environment, EnvironmentVariablesImpl.UseSharedCompilationEnvVarName)?.equals("true", true) ?: false) "true" else "false"
        yield(CommandLineEnvironmentVariable(UseSharedCompilationEnvVarName, useSharedCompilation))
        yieldAll(_nugetEnvironmentVariables.getVariables(sdkVersion))

        val home = if (_environment.os == OSType.WINDOWS) UserProfileEnvVar else HomeEnvVar
        if (_environment.tryGetVariable(home).isNullOrEmpty()) {
            yield(CommandLineEnvironmentVariable(home, System.getProperty("user.home")))
        }

        if (_virtualContext.targetOSType != OSType.WINDOWS) {
            if (_virtualContext.isVirtual && _environment.os == OSType.WINDOWS) {
                LOG.debug("Override temp environment variables by empty values")
                yieldAll(getTempDirVariables())
            } else {
                val tempPath = _pathsService.getPath(PathType.BuildTemp).path
                if (tempPath.length <= 60) {
                    LOG.debug("Do not override temp environment variables")
                }
                else {
                    // try to find default /tmp
                    if (_fileSystemService.isExists(defaultTemp) && _fileSystemService.isDirectory(defaultTemp)) {
                        LOG.debug("Override temp environment variables by '${defaultTemp.path}'")
                        yieldAll(getTempDirVariables(defaultTemp.path))
                    } else {
                        // create short TeamCity temp
                        if (!_fileSystemService.isExists(customTeamCityTemp)) {
                            _fileSystemService.createDirectory(customTeamCityTemp)
                        }

                        LOG.debug("Override temp environment variables by '${customTeamCityTemp}'")
                        yieldAll(getTempDirVariables(customTeamCityTemp.path))
                    }
                }
            }
        }
    }

    companion object {
        private val LOG = Logger.getLogger(EnvironmentVariablesImpl::class.java)

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

        internal fun getTempDirVariables(tempPath: String = "") = sequenceOf(
                CommandLineEnvironmentVariable("TEMP", tempPath),
                CommandLineEnvironmentVariable("TMP", tempPath),
                CommandLineEnvironmentVariable("TMPDIR", tempPath))

        internal val defaultTemp = File("/tmp")
        internal val customTeamCityTemp = File("~/teamcity_temp")
    }
}