package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetLoggerProvider
import jetbrains.buildServer.dotnet.Logger
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.FileSystemService
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class VSTestLoggerArgumentsProvider(
        private val _dotnetLoggerProvider: DotnetLoggerProvider)
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