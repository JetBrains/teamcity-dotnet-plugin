

package jetbrains.buildServer.dotnet.commands.vstest

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class VSTestLoggerArgumentsProvider(
    private val _loggerResolver: LoggerResolver,
    private val _loggerParameters: LoggerParameters,
    private val _virtualContext: VirtualContext)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(CommandLineArgument("/logger:logger://teamcity", CommandLineArgumentType.Infrastructural))
            yield(CommandLineArgument("/TestAdapterPath:${_virtualContext.resolvePath(it.canonicalPath)}", CommandLineArgumentType.Infrastructural))
            yield(CommandLineArgument("/logger:console;verbosity=${_loggerParameters.vsTestVerbosity.id.lowercase()}", CommandLineArgumentType.Infrastructural))
        }
    }
}