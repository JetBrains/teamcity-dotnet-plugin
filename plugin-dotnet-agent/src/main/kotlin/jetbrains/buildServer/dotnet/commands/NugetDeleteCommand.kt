

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.util.StringUtil

class NugetDeleteCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: DotnetToolResolver,
    private val _resultsObserver: Observer<CommandResultEvent>,
): DotnetCommandBase(_parametersService, _resultsObserver) {
    override val commandType = DotnetCommandType.NuGetDelete

    override val command = sequenceOf("nuget", "delete")

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        parameters(DotnetConstants.PARAM_NUGET_PACKAGE_ID)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(StringUtil.split(it).map { CommandLineArgument(it) })
            }
        }

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

        yield(CommandLineArgument("--non-interactive", CommandLineArgumentType.Infrastructural))
        yield(CommandLineArgument("--force-english-output", CommandLineArgumentType.Infrastructural))

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}