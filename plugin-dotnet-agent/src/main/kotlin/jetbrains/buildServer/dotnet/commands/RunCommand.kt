

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class RunCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: DotnetToolResolver,
) : DotnetCommandBase(_parametersService) {

    override val commandType = DotnetCommandType.Run

    override val command = sequenceOf("run")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map {
            TargetArguments(sequenceOf(CommandLineArgument("--project"), CommandLineArgument(it.target.path, CommandLineArgumentType.Target)))
        }

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--runtime"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_SKIP_BUILD, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-build"))
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}