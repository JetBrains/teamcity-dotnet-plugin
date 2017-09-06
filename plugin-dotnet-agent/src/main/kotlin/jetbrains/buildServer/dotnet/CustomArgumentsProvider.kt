package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet for custom arguments.
 */

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class CustomArgumentsProvider(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProvider {

    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
                yieldAll(_argumentsService.split(it).map { CommandLineArgument(it) })
            }
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}