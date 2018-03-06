package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import kotlin.coroutines.experimental.buildSequence

abstract class DotnetCommandBase(private val _parametersService: ParametersService) : DotnetCommand {
    override fun isSuccessful(result: CommandLineResult): Boolean = result.exitCode == 0

    protected fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    protected fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue

    override val environmentBuilders: Sequence<EnvironmentBuilder>
        get() = buildSequence { return@buildSequence }
}