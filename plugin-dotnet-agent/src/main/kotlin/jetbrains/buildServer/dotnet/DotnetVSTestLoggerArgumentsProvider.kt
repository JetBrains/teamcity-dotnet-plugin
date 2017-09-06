package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetVSTestLoggerArgumentsProvider(
        private val _dotnetLoggerProvider: DotnetLogger)
    : ArgumentsProvider {

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            val loggerPath = _dotnetLoggerProvider.tryGetToolPath(Logger.VSTestLogger15);
            if (loggerPath != null) {
                yield(CommandLineArgument("-l=TeamCity"))
                yield(CommandLineArgument("-a=${loggerPath.parentFile.absolutePath}"))
            }
        }
}