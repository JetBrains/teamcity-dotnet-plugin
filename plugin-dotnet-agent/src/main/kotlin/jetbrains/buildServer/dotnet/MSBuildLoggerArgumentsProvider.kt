package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class MSBuildLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver)
    : ArgumentsProvider {

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            val loggerPath = _loggerResolver.resolve(ToolType.MSBuild);
            if (loggerPath != null) {
                yield(CommandLineArgument("/noconsolelogger"))
                yield(CommandLineArgument("/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${loggerPath.absolutePath};TeamCity"))
            }
        }
}