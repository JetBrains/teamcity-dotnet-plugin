

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class MSBuildLoggerArgumentsProvider(
    private val _loggerResolver: LoggerResolver,
    private val _loggerParameters: LoggerParameters,
    private val _virtualContext: VirtualContext)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("/noconsolelogger"))
        val parameters = sequence<String> {
            yield("/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${_virtualContext.resolvePath(_loggerResolver.resolve(
                ToolType.MSBuild
            ).canonicalPath)}")
            yield("TeamCity")

            _loggerParameters.msBuildLoggerVerbosity?.let {
                yield("verbosity=${it.id.lowercase()}")
            }

            yieldAll(_loggerParameters.getAdditionalLoggerParameters(context))

            _loggerParameters.msBuildParameters.let {
                if (it.isNotBlank()) {
                    yield(it);
                }
            }
        }

        yield(CommandLineArgument("\"${parameters.joinToString(";")}\"", CommandLineArgumentType.Infrastructural))
    }
}