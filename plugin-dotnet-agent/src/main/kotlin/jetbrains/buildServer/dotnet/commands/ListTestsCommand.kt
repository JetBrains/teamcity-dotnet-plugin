package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*

public class ListTestsCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    override val toolResolver: DotnetToolResolver,
    private val _targetService: TargetService,
    private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _assemblyArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _targetArgumentsProvider: TargetArgumentsProvider
) : DotnetCommandBase(_parametersService) {
    override val commandType = DotnetCommandType.ListTests

    override val commandWords = sequenceOf("test")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getCommandSpecificArguments(context: DotnetBuildContext): Sequence<CommandLineArgument> =
        sequenceOf(
            "--list-tests",
            "--settings",
            "--",
            // NUnit should be set up to print fully qualified names
            "NUnit.DisplayName=FullName",
        ).map(::CommandLineArgument)
}