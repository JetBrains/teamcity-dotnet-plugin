package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class MSBuildVSTestLoggerParametersProvider(
        private val _pathsService: PathsService,
        private val _loggerResolver: LoggerResolver)
    : MSBuildParametersProvider {

    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(MSBuildParameter("VSTestLogger", "logger://teamcity"))
                yield(MSBuildParameter("VSTestTestAdapterPath", _pathsService.getPath(PathType.WorkingDirectory).absolutePath))
            }
        }
}