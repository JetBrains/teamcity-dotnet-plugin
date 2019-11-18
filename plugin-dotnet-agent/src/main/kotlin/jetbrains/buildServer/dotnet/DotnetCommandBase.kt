package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyObserver

abstract class DotnetCommandBase(
        private val _parametersService: ParametersService,
        override val resultsObserver: Observer<CommandResultEvent> = emptyObserver())
    : DotnetCommand {
    protected fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    protected fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue

    override val environmentBuilders: Sequence<EnvironmentBuilder> get() = emptySequence()
}