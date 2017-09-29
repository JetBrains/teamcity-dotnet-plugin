package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class VSTestLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver)
    : ArgumentsProvider {

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(CommandLineArgument("/logger:logger://teamcity"))
                yield(CommandLineArgument("/TestAdapterPath:${it.absolutePath}"))
            }
        }
}