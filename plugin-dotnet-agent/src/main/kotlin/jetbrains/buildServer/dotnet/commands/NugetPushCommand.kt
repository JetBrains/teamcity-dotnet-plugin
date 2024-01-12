

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.rx.Observer

class NugetPushCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: DotnetToolResolver,
    private val _resultsObserver: Observer<CommandResultEvent>,
) : DotnetCommandBase(_parametersService, _resultsObserver) {

    override val commandType = DotnetCommandType.NuGetPush

    override val command = sequenceOf("nuget", "push")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_NUGET_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--api-key"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--source"))
                yield(CommandLineArgument(it))
            }
        }

        if (parameters(DotnetConstants.PARAM_NUGET_NO_SYMBOLS, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-symbols"))

            if (context.toolVersion < Version.NoArgsForNuGetPushNoSymbolsParameterVersion) {
                yield(CommandLineArgument("true"))
            }
        }

        yield(CommandLineArgument("--force-english-output", CommandLineArgumentType.Infrastructural))

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}