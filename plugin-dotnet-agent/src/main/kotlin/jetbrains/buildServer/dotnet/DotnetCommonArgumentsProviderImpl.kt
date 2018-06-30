@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

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
        private val _msBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _msBuildParameterConverter: MSBuildParameterConverter)
    : DotnetCommonArgumentsProvider {
    override val arguments: Sequence<CommandLineArgument>
        get() = buildSequence {
            yieldAll(_customArgumentsProvider.arguments)
            val avoidUsingRspFiles = _parametersService.tryGetParameter(ParameterType.Configuration, PARAM_RSP)?.equals("false", true) ?: false
            if (!avoidUsingRspFiles) {
                yieldAll(_responseFileArgumentsProvider.arguments)
            } else {
                yieldAll(_msBuildLoggerArgumentsProvider.arguments)
                yieldAll(_msBuildVSTestLoggerParametersProvider.parameters.map { CommandLineArgument(_msBuildParameterConverter.convert(it)) })
            }
        }
}