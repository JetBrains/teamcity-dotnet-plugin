/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger
import java.io.File

class EnvironmentVariablesImpl(
        private val _environment: Environment,
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _dotnetToolEnvironment: ToolEnvironment,
        private val _virtualContext: VirtualContext,
        private val _credentialProviderSelector: NugetCredentialProviderSelector)
    : EnvironmentVariables {
    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        yieldAll(defaultVariables)

        _dotnetToolEnvironment.cachePaths.firstOrNull()?.let {
            yield(CommandLineEnvironmentVariable(DotnetToolEnvironment.NUGET_PACKAGES_ENV_VAR, _virtualContext.resolvePath(it.path)))
        }

        _credentialProviderSelector.trySelect(sdkVersion)?.let {
            LOG.debug("Set credentials plugin paths to $it")
            yield(CommandLineEnvironmentVariable(NUGET_PLUGIN_PATH_ENV_VAR, it))
        }

        val home = if (_environment.os == OSType.WINDOWS) USERPROFILE_ENV_VAR else HOME_ENV_VAR
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
                        // create short TemamCity temp
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

        private const val NUGET_PLUGIN_PATH_ENV_VAR = "NUGET_PLUGIN_PATHS"
        private const val USERPROFILE_ENV_VAR = "USERPROFILE"
        private const val HOME_ENV_VAR = "HOME"

        internal val defaultVariables = sequenceOf(
                CommandLineEnvironmentVariable("UseSharedCompilation", "false"),
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