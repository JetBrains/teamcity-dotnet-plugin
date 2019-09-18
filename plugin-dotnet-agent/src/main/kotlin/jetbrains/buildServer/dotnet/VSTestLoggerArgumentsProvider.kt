package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.VirtualContext
import java.io.File

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class VSTestLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver,
        private val _loggerParameters: LoggerParameters,
        private val _virtualContext: VirtualContext)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(CommandLineArgument("/logger:logger://teamcity"))
            yield(CommandLineArgument("/TestAdapterPath:${_virtualContext.resolvePath(it.canonicalPath)}"))
            yield(CommandLineArgument("/logger:console;verbosity=${_loggerParameters.vsTestVerbosity.id.toLowerCase()}"))
        }
    }
}