package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.messages.serviceMessages.Message
import java.io.File

class DotnetBuildContextFactoryImpl(
        private val _pathService: PathsService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _parametersService: ParametersService,
        private val _loggerService: LoggerService)
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
        var sdk = sdkInfo.sdks.firstOrNull() { it.version == sdkInfo.version }
        if (sdk == null) {
            sdk = sdkInfo.sdks.sortedByDescending { it.version }.firstOrNull() ?: throw RunBuildException(".NET SDK was not found.")
            _loggerService.writeBuildProblem(BuildProblemData.createBuildProblem("dotnet_sdk_not_found", BuildProblemData.TC_ERROR_MESSAGE_TYPE, "A compatible .NET SDK version ${if(sdkInfo.version != Version.Empty) "${sdkInfo.version} " else ""}from [$workingDirectory] was not found."));
        }

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