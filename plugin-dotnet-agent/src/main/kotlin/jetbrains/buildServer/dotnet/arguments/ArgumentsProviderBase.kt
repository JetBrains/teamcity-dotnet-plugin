package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.ArgumentsProvider
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.CommandLineArgument
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

abstract class ArgumentsProviderBase(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProvider {

    override fun getArguments(): Sequence<CommandLineArgument> = buildSequence {
        yieldAll(getArgumentStrings().map { CommandLineArgument(it) })

        parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
            yieldAll(_argumentsService.parseToStrings(it).map { CommandLineArgument(it) })
        }
    }

    protected fun parameters(parameterName: String): String? =
            _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    protected fun parameters(parameterName: String, defaultValue: String): String
            = parameters(parameterName) ?: defaultValue

    protected abstract fun getArgumentStrings(): Sequence<String>
}