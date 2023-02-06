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

package jetbrains.buildServer.dotnet.commands.nuget

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.EnvironmentVariables
import jetbrains.buildServer.dotnet.Verbosity
import java.io.File

class NugetEnvironmentVariables(
    private val _environment: Environment,
    private val _parametersService: ParametersService,
    private val _pathsService: PathsService,
    private val _virtualContext: VirtualContext,
    private val _credentialProviderSelector: NugetCredentialProviderSelector,
    private val _nugetEnvironment: NugetEnvironment
)
    : EnvironmentVariables {

    private val _basePath get() = File(_pathsService.getPath(PathType.System), "dotnet")

    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        var varsToOverride = _parametersService.tryGetParameter(ParameterType.Configuration,
            DotnetConstants.PARAM_OVERRIDE_NUGET_VARS
        )
            ?.uppercase()
                ?.split(';')
                ?.map { it.trim() }
                ?.toHashSet()
                ?: emptySet<String>()

        yieldEnvVar(varsToOverride, FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR) { "" }

        if (_nugetEnvironment.allowInternalCaches) {
            yieldEnvVar(varsToOverride, NUGET_HTTP_CACHE_PATH_ENV_VAR) { _virtualContext.resolvePath(File(_basePath, ".http").canonicalPath) }
            yieldEnvVar(varsToOverride, NUGET_PACKAGES_ENV_VAR) { _virtualContext.resolvePath(File(_basePath, ".nuget").canonicalPath) }
        }

        _credentialProviderSelector.trySelect(sdkVersion)?.let {
            val credentialProvider = it
                yieldEnvVar(varsToOverride, NUGET_PLUGIN_PATH_ENV_VAR) {
                    _virtualContext.resolvePath(credentialProvider)
            }
        }

        _parametersService
                .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                ?.trim()
                ?.let { Verbosity.tryParse(it) }
                ?.let {
                    yieldEnvVar(varsToOverride, NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR) { it.id }
                }
    }

    private suspend fun SequenceScope<CommandLineEnvironmentVariable>.yieldEnvVar(
            varsToOverride: Set<String>,
            environmentVariableName: String, valueProvider: () -> String) {
        if (varsToOverride.size == 0 || varsToOverride.contains(environmentVariableName.uppercase())) {
            if (_environment.tryGetVariable(environmentVariableName).isNullOrBlank() && _parametersService.tryGetParameter(ParameterType.Environment, environmentVariableName).isNullOrBlank()) {
                yield(CommandLineEnvironmentVariable(environmentVariableName, valueProvider()))
            }
        }
    }

    companion object {
        internal const val FORCE_NUGET_EXE_INTERACTIVE_ENV_VAR = "FORCE_NUGET_EXE_INTERACTIVE"
        internal const val NUGET_HTTP_CACHE_PATH_ENV_VAR = "NUGET_HTTP_CACHE_PATH"
        internal const val NUGET_PACKAGES_ENV_VAR = "NUGET_PACKAGES"
        internal const val NUGET_PLUGIN_PATH_ENV_VAR = "NUGET_PLUGIN_PATHS"
        internal const val NUGET_RESTORE_MSBUILD_VERBOSITY_ENV_VAR = "NUGET_RESTORE_MSBUILD_VERBOSITY"
    }
}