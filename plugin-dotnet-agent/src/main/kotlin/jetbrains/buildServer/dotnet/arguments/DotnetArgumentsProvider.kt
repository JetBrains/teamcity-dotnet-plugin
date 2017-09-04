package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import jetbrains.buildServer.util.StringUtil
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _MSBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _verbosityArgumentsProvider: ArgumentsProvider,
        commandArgumentsProviders: List<DotnetCommandArgumentsProvider>)
    : ArgumentsProvider {

    private val _argumentsProviders: Map<String, DotnetCommandArgumentsProvider> = commandArgumentsProviders.associateBy({ it.command.command }, { it })

    override fun getArguments(): Sequence<CommandLineArgument> {
        val commandName = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
        if (commandName.isNullOrBlank()) {
            throw RunBuildException("Dotnet command name is empty")
        }

        val commandSpecificArgumentsProvider = _argumentsProviders[commandName] ?: throw RunBuildException("Unknown dotnet command \"$commandName\"")
        return buildSequence {
            // command
            yieldAll(commandSpecificArgumentsProvider.command.args.map { CommandLineArgument(it) })
            // command specific arguments
            yieldAll(commandSpecificArgumentsProvider.getArguments())
            // verbosity level
            yieldAll(_verbosityArgumentsProvider.getArguments())
            // custom arguments
            yieldAll(_customArgumentsProvider.getArguments())
            // logger
            yieldAll(_MSBuildLoggerArgumentsProvider.getArguments())
        };
    }
}