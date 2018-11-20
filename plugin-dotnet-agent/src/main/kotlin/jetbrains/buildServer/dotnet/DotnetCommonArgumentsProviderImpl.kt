package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_RSP

class DotnetCommonArgumentsProviderImpl(
        private val _parametersService: ParametersService,
        private val _responseFileArgumentsProvider: ArgumentsProvider,
        private val _customArgumentsProvider: ArgumentsProvider,
        private val _msBuildVSTestLoggerParametersProvider: MSBuildParametersProvider,
        private val _msBuildLoggerArgumentsProvider: ArgumentsProvider,
        private val _sharedCompilationArgumentsProvider: ArgumentsProvider,
        private val _msBuildParameterConverter: MSBuildParameterConverter)
    : DotnetCommonArgumentsProvider {
    override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> = sequence {
        yieldAll(_customArgumentsProvider.getArguments(context))

        val avoidUsingRspFiles = _parametersService.tryGetParameter(ParameterType.Configuration, PARAM_RSP)?.equals("false", true) ?: false
        if (!avoidUsingRspFiles) {
            yieldAll(_responseFileArgumentsProvider.getArguments(context))
        } else {
            yieldAll(_msBuildLoggerArgumentsProvider.getArguments(context))
            yieldAll(_msBuildVSTestLoggerParametersProvider.getParameters(context).map { CommandLineArgument(_msBuildParameterConverter.convert(it)) })
            yieldAll(_sharedCompilationArgumentsProvider.getArguments(context))
        }
    }
}