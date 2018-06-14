package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class VSTestLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver,
        private val _loggerParameters: LoggerParameters)
    : ArgumentsProvider {

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(CommandLineArgument("/logger:logger://teamcity"))
                yield(CommandLineArgument("/TestAdapterPath:${it.absolutePath}"))
                yield(CommandLineArgument("/logger:console;verbosity=${_loggerParameters.VSTestVerbosity.id.toLowerCase()}"))
            }
        }
}