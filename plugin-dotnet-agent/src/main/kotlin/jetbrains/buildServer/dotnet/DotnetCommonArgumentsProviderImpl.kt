package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_RSP
import kotlin.coroutines.experimental.buildSequence

class DotnetCommonArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _responseFileArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _msBuildVSTestLoggerParametersProvider: MSBuildParametersProvider,
        private val _msBuildParameterConverter: MSBuildParameterConverter)
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
            val useRspFiles = _parametersService.tryGetParameter(ParameterType.Configuration, PARAM_RSP)?.let { it.equals("false", true) } ?: true;
            if (useRspFiles) {
                yieldAll(_responseFileArgumentsProvider.arguments)
            }
            else {
                yieldAll(_msBuildVSTestLoggerParametersProvider.parameters.map { CommandLineArgument( _msBuildParameterConverter.convert(it)) })
            }
        }

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)
}