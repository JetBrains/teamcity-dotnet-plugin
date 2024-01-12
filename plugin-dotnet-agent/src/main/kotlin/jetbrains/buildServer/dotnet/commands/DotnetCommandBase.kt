

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommand
import jetbrains.buildServer.dotnet.EnvironmentBuilder
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyObserver

abstract class DotnetCommandBase(
    private val _parametersService: ParametersService,
    override val resultsObserver: Observer<CommandResultEvent> = emptyObserver()
) : DotnetCommand {
    override val isAuxiliary = false

    override val title = ""

    protected fun parameters(parameterName: String): String? =
        _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    protected fun parameters(parameterName: String, defaultValue: String): String =
        _parametersService
            .tryGetParameter(ParameterType.Runner, parameterName)
            ?: defaultValue

    override val environmentBuilders: List<EnvironmentBuilder> get() = emptyList()
}