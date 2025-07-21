package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class RestorePackagesPathManager(
    private val _parametersService: ParametersService,
    private val _dotnetRunnerCacheDirectoryProvider: DotnetRunnerCacheDirectoryProvider
) {

    fun shouldOverrideRestorePackagesPath(): Boolean {
        return _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.RESTORE_PACKAGES_PATH_OVERRIDE_ENABLED)
            ?.lowercase()
            ?.toBooleanStrictOrNull()
            ?: true
    }

    fun getRestorePackagesPathLocation(agentConfiguration: BuildAgentConfiguration): File {
        val localDotnetCacheDirectory = _dotnetRunnerCacheDirectoryProvider.getDotnetRunnerCacheDirectory(agentConfiguration)
        val restorePackagesPath = File(localDotnetCacheDirectory, NUGET_PACKAGES_DIR)

        if (!restorePackagesPath.exists()) {
            restorePackagesPath.mkdirs()
        }

        return restorePackagesPath
    }

    companion object {
        val NUGET_PACKAGES_DIR = "nuget.packages"
    }
}