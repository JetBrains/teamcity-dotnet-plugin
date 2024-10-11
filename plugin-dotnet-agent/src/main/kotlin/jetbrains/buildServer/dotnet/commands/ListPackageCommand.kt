package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

class ListPackageCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    override val toolResolver: DotnetToolResolver
) : DotnetCommandBase(_parametersService) {

    override val commandType = DotnetCommandType.ListPackage

    override val command = sequenceOf("list")

    override val title = "list the package references for a project"

    override val isAuxiliary: Boolean = true

    override val targetArguments: Sequence<TargetArguments>
        get() = emptySequence()

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("package"))
        // The --format option is only available starting from version 7.0.200
        // see https://learn.microsoft.com/en-us/dotnet/core/tools/dotnet-list-package
        yield(CommandLineArgument("--format=json"))
        yield(CommandLineArgument("--output-version=1"))
        yield(CommandLineArgument("--include-transitive"))
    }
}