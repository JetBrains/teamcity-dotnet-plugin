package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument

/**
 * Provides arguments to dotnet related to TeamCity logger.
 */

class MSBuildLoggerArgumentsProvider(
        private val _loggerResolver: LoggerResolver,
        private val _loggerParameters: LoggerParameters)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("/noconsolelogger"))
        val parameters = mutableListOf<String>(
                "/l:TeamCity.MSBuild.Logger.TeamCityMSBuildLogger,${_loggerResolver.resolve(ToolType.MSBuild).absolutePath}",
                "TeamCity")

        _loggerParameters.msBuildLoggerVerbosity?.let {
            parameters.add("verbosity=${it.id.toLowerCase()}")
        }

        _loggerParameters.msBuildParameters.let {
            if (it.isNotBlank()) {
                parameters.add(it);
            }
        }

        yield(CommandLineArgument(parameters.joinToString(";")))
    }
}