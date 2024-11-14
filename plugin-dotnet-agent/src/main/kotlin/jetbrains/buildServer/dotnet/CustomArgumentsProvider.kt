package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

/**
 * Provides arguments to dotnet for custom arguments.
 */

class CustomArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProvider {

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
            yieldAll(_argumentsService.split(it).map { CommandLineArgument(it, CommandLineArgumentType.Custom) })
        }
    }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}