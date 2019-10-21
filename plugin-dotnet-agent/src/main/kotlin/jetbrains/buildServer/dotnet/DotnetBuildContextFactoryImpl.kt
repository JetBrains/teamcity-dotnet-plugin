package jetbrains.buildServer.dotnet

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.messages.serviceMessages.Message
import org.apache.log4j.Logger
import java.io.File

class DotnetBuildContextFactoryImpl(
        private val _pathService: PathsService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo,
        private val _parametersService: ParametersService,
        private val _loggerService: LoggerService,
        private val _toolProvider: ToolProvider)
    : DotnetBuildContextFactory {
    override fun create(command: DotnetCommand): DotnetBuildContext {
        var dotnetPath = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
        LOG.debug("${DotnetConstants.CONFIG_PATH} is \"$dotnetPath\"")
        if (dotnetPath.isNullOrBlank()) {
            // Detect .NET CLI path in place (in the case of docker wrapping).
            LOG.debug("Try to find ${DotnetConstants.EXECUTABLE} executable.")
            dotnetPath = _toolProvider.getPath(DotnetConstants.EXECUTABLE)
            LOG.debug("${DotnetConstants.EXECUTABLE} is \"$dotnetPath\"")
            if (dotnetPath.isNullOrBlank()) {
                throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
            }
        }

        val workingDirectory = _pathService.getPath(PathType.WorkingDirectory)
        val sdkInfo = _dotnetCliToolInfo.getInfo(File(dotnetPath), workingDirectory)
        LOG.info(".NET Core SDK version is ${sdkInfo.version} for the directory \"$workingDirectory\"")
        var sdk = sdkInfo.sdks.firstOrNull() { it.version == sdkInfo.version }
        if (sdk == null) {
            sdk = sdkInfo.sdks.maxBy { it.version } ?: throw RunBuildException(".NET SDK was not found.")
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
        private val LOG = Logger.getLogger(DotnetBuildContextFactoryImpl::class.java)
    }
}