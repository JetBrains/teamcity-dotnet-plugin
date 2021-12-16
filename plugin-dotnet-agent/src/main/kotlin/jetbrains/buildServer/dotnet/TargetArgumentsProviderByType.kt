package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_SINGLE_SESSION
import java.io.File

class TargetArgumentsProviderByType(
        private val _parametersService: ParametersService,
        private val _targetTypeProvider: TargetTypeProvider):
        TargetArgumentsProvider {

    override fun getTargetArguments(targets: Sequence<CommandTarget>) =
            if (isEnabled) splitByTargetType(targets) else splitByDefault(targets)

    private val isEnabled: Boolean
        get() = _parametersService.tryGetParameter(ParameterType.Runner, PARAM_SINGLE_SESSION)?.trim()?.toBoolean() ?: false

    private fun splitByTargetType(targets: Sequence<CommandTarget>): Sequence<TargetArguments> =
            targets
                    .groupBy { _targetTypeProvider.getTargetType(File(it.target.path)) }
                    .asSequence()
                    .map {
                        when (it.key) {
                            CommandTargetType.Assembly -> sequenceOf(TargetArguments(it.value.map { CommandLineArgument(it.target.path, CommandLineArgumentType.Target) }.asSequence()))
                            else -> splitByDefault(it.value.asSequence())
                        }
                    }.flatMap { it }

    private fun splitByDefault(targets: Sequence<CommandTarget>) =
            targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }
}