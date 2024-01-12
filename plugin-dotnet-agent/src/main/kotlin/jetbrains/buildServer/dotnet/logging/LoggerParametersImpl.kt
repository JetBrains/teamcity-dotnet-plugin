

package jetbrains.buildServer.dotnet.logging

import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity

class LoggerParametersImpl(
    private val _parametersService: ParametersService,
    private val _customArgumentsProvider: ArgumentsProvider
)
    : LoggerParameters {

    override val paramVerbosity: Verbosity?
        get() = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                ?.trim()
                ?.let {
                    Verbosity.tryParse(it)
                }

    override val msBuildLoggerVerbosity: Verbosity?
        get() = paramVerbosity

    override val vsTestVerbosity: Verbosity
        get() = paramVerbosity?.let {
            when (it) {
                Verbosity.Quiet, Verbosity.Minimal -> Verbosity.Normal
                else -> it
            }
        }
                ?: Verbosity.Normal

    override val msBuildParameters: String
        get() = _parametersService.tryGetParameter(
            ParameterType.Configuration,
            DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS
        ) ?: defaultMsBuildLoggerParams

    override fun getAdditionalLoggerParameters(context: DotnetCommandContext) =
            _customArgumentsProvider
                    .getArguments(context)
                    .mapNotNull { LoggerParamRegex.find(it.value) }
                    .map { it.groupValues[2] }
                    .flatMap { it.split(';').asSequence() }
                    .filter { !it.isNullOrBlank() }

    companion object {
        const val defaultMsBuildLoggerParams = "plain";
        private val LoggerParamRegex = Regex("^\\s*([-/]consoleloggerparameters|[-/]clp):(.+?)\\s*\$", RegexOption.IGNORE_CASE)
    }
}