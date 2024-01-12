

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class RestoreCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _argumentsService: ArgumentsService,
    private val _targetService: TargetService,
    private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
    override val toolResolver: DotnetToolResolver,
) : DotnetCommandBase(_parametersService) {
    override val commandType  = DotnetCommandType.Restore

    override val command = sequenceOf("restore")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_NUGET_PACKAGES_DIR)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--packages"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCES)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_RUNTIME)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument("--runtime"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_CONFIG_FILE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configfile"))
                yield(CommandLineArgument(it))
            }
        }

        yieldAll(_commonArgumentsProvider.getArguments(context))
    }
}