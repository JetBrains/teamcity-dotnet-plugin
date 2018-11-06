@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService

class DotnetCommandSet(
        private val _parametersService: ParametersService,
        commands: List<DotnetCommand>)
    : CommandSet {

    private val _knownCommands: Map<String, DotnetCommand> = commands.associateBy({ it.commandType.id }, { it })

    override val commands: Sequence<DotnetCommand>
        get() = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)?.let {
            _knownCommands[it]?.let { command ->
                getTargetArguments(command).asSequence().map {
                    val targetArguments = TargetArguments(it.arguments.toList().asSequence())
                    CompositeCommand(command, targetArguments)
                }
            }
        } ?: emptySequence()

    private fun getTargetArguments(command: DotnetCommand) = sequence {
        var hasTargets = false
        for (targetArguments in command.targetArguments) {
            yield(targetArguments)
            hasTargets = true
        }

        if (!hasTargets) {
            yield(TargetArguments(emptySequence()))
        }
    }

    class CompositeCommand(
            private val _command: DotnetCommand,
            private val _targetArguments: TargetArguments)
        : DotnetCommand {

        override val commandType: DotnetCommandType
            get() = _command.commandType

        override val toolResolver: ToolResolver
            get() = _command.toolResolver

        override fun getArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
                sequence {
                    if (_command.toolResolver.isCommandRequired) {
                        // command
                        yieldAll(_command.commandType.id.split('-')
                                .filter { it.isNotEmpty() }
                                .map { CommandLineArgument(it) })
                    }

                    // projects
                    yieldAll(_targetArguments.arguments)
                    // command specific arguments
                    yieldAll(_command.getArguments(context))
                }

        override val targetArguments: Sequence<TargetArguments>
            get() = sequenceOf(_targetArguments)

        override val environmentBuilders: Sequence<EnvironmentBuilder>
            get() = _command.environmentBuilders

        override val resultsAnalyzer: ResultsAnalyzer
            get() = _command.resultsAnalyzer
    }
}