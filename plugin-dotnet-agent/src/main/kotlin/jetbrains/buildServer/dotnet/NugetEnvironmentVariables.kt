package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class NugetEnvironmentVariables(
        private val _environment: Environment,
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext,
        private val _credentialProviderSelector: NugetCredentialProviderSelector,
        private val _nugetEnvironment: NugetEnvironment)
    : EnvironmentVariables {

    private val _basePath get() = File(_pathsService.getPath(PathType.System), "dotnet")

    override fun getVariables(sdkVersion: Version): Sequence<CommandLineEnvironmentVariable> = sequence {
        var varsToOverride = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.PARAM_OVERRIDE_NUGET_VARS)
                ?.toUpperCase()
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
        if (varsToOverride.size == 0 || varsToOverride.contains(environmentVariableName.toUpperCase())) {
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