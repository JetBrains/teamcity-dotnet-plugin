package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotnetCommonArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _responseFileArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider)
    : DotnetCommonArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
                Verbosity.tryParse(it)?.let {
                    yield(CommandLineArgument("--verbosity"))
                    yield(CommandLineArgument(it.id))
                }
            }

            yieldAll(_customArgumentsProvider.arguments)
            yieldAll(_responseFileArgumentsProvider.arguments)
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}