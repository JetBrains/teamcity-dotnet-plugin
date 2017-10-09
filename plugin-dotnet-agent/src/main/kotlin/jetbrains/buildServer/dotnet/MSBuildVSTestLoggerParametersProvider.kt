package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class MSBuildVSTestLoggerParametersProvider(
        private val _loggerResolver: LoggerResolver)
    : MSBuildParametersProvider {

    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(MSBuildParameter("VSTestLogger", "logger://teamcity"))
                yield(MSBuildParameter("VSTestTestAdapterPath", "."))
            }
        }
}