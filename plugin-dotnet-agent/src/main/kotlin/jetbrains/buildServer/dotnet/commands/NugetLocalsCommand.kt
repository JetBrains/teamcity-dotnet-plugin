package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class NugetLocalsCommand(
    _parametersService: ParametersService,
    override val toolResolver: DotnetToolResolver,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService
) : DotnetCommandBase(_parametersService) {

    override val commandType = DotnetCommandType.NuGetLocals

    override val command = sequenceOf("nuget", "locals")

    override val title = "list local NuGet resources"

    override val isAuxiliary: Boolean = true

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("global-packages"))
        yield(CommandLineArgument("-l"))
        yield(CommandLineArgument("--force-english-output"))
    }
}