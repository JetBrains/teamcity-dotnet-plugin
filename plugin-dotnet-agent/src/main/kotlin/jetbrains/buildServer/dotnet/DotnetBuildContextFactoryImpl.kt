package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotnetBuildContextFactoryImpl(
        private val _pathService: PathsService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _parametersService: ParametersService)
    : DotnetBuildContextFactory {
    override fun create(command: DotnetCommand): DotnetBuildContext {
        val dotnetPath = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
        LOG.debug("$DotnetConstants.CONFIG_PATH is \"$dotnetPath\"")
        if (dotnetPath.isNullOrBlank()) {
            throw RunBuildException("Cannot find the configuration parameter \"$DotnetConstants.CONFIG_PATH\".")
        }

        val workingDirectory = _pathService.getPath(PathType.WorkingDirectory)
        val sdkInfo = _dotnetCliToolInfo.getInfo(File(dotnetPath), workingDirectory)
        LOG.info(".NET Core SDK version is ${sdkInfo.version} for the directory \"$workingDirectory\"")
        val sdk = sdkInfo.sdks.firstOrNull() { it.version == sdkInfo.version } ?: throw RunBuildException("Cannot find the .NET Core SDK v${sdkInfo.version} for the directory \"$workingDirectory\".")

        return DotnetBuildContext(
                workingDirectory,
                command,
                sdk,
                getVerbosityLevel(),
                sdkInfo.sdks.toSet())
    }

    private fun getVerbosityLevel(): Verbosity? =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)
            }

    companion object {
        private val LOG = Logger.getInstance(DotnetBuildContextFactoryImpl::class.java.name)
    }
}