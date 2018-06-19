package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

abstract class DotnetCommandBase(
        private val _parametersService: ParametersService)
    : DotnetCommand {
    protected fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    protected fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue

    override val environmentBuilders: Sequence<EnvironmentBuilder> get() = emptySequence()
}