package jetbrains.buildServer.dotnet

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
    override fun create(command: DotnetCommand): DotnetBuildContext =
            DotnetBuildContext(
                    command,
                    getVerbosityLevel(),
                    getSdks().toSet())

    private fun getSdks(): Sequence<DotnetSdk> =
            sequence {
                _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)?.let { path ->
                    yieldAll(_dotnetCliToolInfo.getInfo(File(path), _pathService.getPath(PathType.WorkingDirectory)).sdks)
                }
            }

    private fun getVerbosityLevel(): Verbosity? =
            _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)
            }
}