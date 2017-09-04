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
class DotnetArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _MSBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _verbosityArgumentsProvider: ArgumentsProvider,
        _buildArgumentsProvider: ArgumentsProvider,
        _packArgumentsProvider: ArgumentsProvider,
        _publishArgumentsProvider: ArgumentsProvider,
        _restoreArgumentsProvider: ArgumentsProvider,
        _runArgumentsProvider: ArgumentsProvider,
        _testArgumentsProvider: ArgumentsProvider,
        _nugetPushArgumentsProvider: ArgumentsProvider,
        _nugetDeleteArgumentsProvider: ArgumentsProvider)
    : ArgumentsProvider {

    private val myArgumentsProviders: Map<String, ArgumentsProvider> = mapOf(
            DotnetConstants.COMMAND_BUILD to _buildArgumentsProvider,
            DotnetConstants.COMMAND_PACK to _packArgumentsProvider,
            DotnetConstants.COMMAND_PUBLISH to _publishArgumentsProvider,
            DotnetConstants.COMMAND_RESTORE to _restoreArgumentsProvider,
            DotnetConstants.COMMAND_RUN to _runArgumentsProvider,
            DotnetConstants.COMMAND_TEST to _testArgumentsProvider,
            DotnetConstants.COMMAND_NUGET_PUSH to _nugetPushArgumentsProvider,
            DotnetConstants.COMMAND_NUGET_DELETE to _nugetDeleteArgumentsProvider)

    override fun getArguments(): Sequence<CommandLineArgument> {
        val commandName = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
        if (commandName.isNullOrBlank()) {
            throw RunBuildException("Dotnet command name is empty")
        }

        val commandSpecificArgumentsProvider = myArgumentsProviders[commandName] ?: throw RunBuildException("Unable to construct arguments for dotnet command $commandName")

        return buildSequence {
            yieldAll(commandSpecificArgumentsProvider.getArguments())
            yieldAll(_verbosityArgumentsProvider.getArguments())
            yieldAll(_customArgumentsProvider.getArguments())
            yieldAll(_MSBuildLoggerArgumentsProvider.getArguments())
        };
    }
}