@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

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

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = buildSequence {
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(CommandLineArgument("/logger:logger://teamcity"))
            yield(CommandLineArgument("/TestAdapterPath:${it.absolutePath}"))
            yield(CommandLineArgument("/logger:console;verbosity=${_loggerParameters.vsTestVerbosity.id.toLowerCase()}"))
        }
    }
}