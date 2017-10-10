package jetbrains.buildServer.dotnet

import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

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