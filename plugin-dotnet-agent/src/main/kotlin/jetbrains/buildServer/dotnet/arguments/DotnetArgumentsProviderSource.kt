package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetArgumentsProviderSource(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _MSBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _verbosityArgumentsProvider: ArgumentsProvider,
        commandArgumentsProviders: List<DotnetCommandArgumentsProvider>)
    : ArgumentsProviderSource {

    private val _argumentsProviders: Map<String, DotnetCommandArgumentsProvider> = commandArgumentsProviders.associateBy({ it.command.command }, { it })

    override fun iterator(): Iterator<ArgumentsProvider> {
        val commandName = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
        if (commandName.isNullOrBlank()) {
            throw RunBuildException("Dotnet command name is empty")
        }

        val commandSpecificArgumentsProvider = _argumentsProviders[commandName] ?: throw RunBuildException("Unknown dotnet command \"$commandName\"")
        return getTargetArguments(commandSpecificArgumentsProvider).map { CompositeArgumentsProvider(getArguments(commandSpecificArgumentsProvider, it)) }.iterator()
    }

    private fun getTargetArguments(commandSpecificArgumentsProvider: DotnetCommandArgumentsProvider): Sequence<TargetArguments>
    {
        return buildSequence {
            var hasTargets = false;
            for(targetArguments in commandSpecificArgumentsProvider.targetArguments)
            {
                yield(targetArguments)
                hasTargets = true
            }

            if (!hasTargets) {
                yield(TargetArguments(emptySequence()))
            }
        }
    }

    private fun getArguments(
            commandSpecificArgumentsProvider: DotnetCommandArgumentsProvider,
            targetArguments: TargetArguments): Sequence<CommandLineArgument> {
        return buildSequence {
            // command
            yieldAll(commandSpecificArgumentsProvider.command.args.map { CommandLineArgument(it) })
            // projects
            yieldAll(targetArguments.arguments)
            // command specific arguments
            yieldAll(commandSpecificArgumentsProvider.arguments)
            // verbosity level
            yieldAll(_verbosityArgumentsProvider.arguments)
            // custom arguments
            yieldAll(_customArgumentsProvider.arguments)
            // logger
            yieldAll(_MSBuildLoggerArgumentsProvider.arguments)
        };
    }

    class CompositeArgumentsProvider(private val _arguments: Sequence<CommandLineArgument>): ArgumentsProvider{
        override val arguments: Sequence<CommandLineArgument>
            get() = _arguments
    }
}