package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class LoggerParametersImpl(
        private val _parametersService: ParametersService)
    : LoggerParameters {

    override val ParamVerbosity: Verbosity?
        get() = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                ?.trim()
                ?.let {
                    Verbosity.tryParse(it)
                }

    override val MSBuildLoggerVerbosity: Verbosity?
        get() = ParamVerbosity

    override val VSTestVerbosity: Verbosity
        get() = ParamVerbosity?.let {
                when(it) {
                    Verbosity.Quiet, Verbosity.Minimal -> Verbosity.Normal
                    else -> it
                }}
                ?: Verbosity.Normal
}