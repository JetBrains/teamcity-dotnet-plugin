package jetbrains.buildServer.dotnet

interface DotnetCommand : ArgumentsProvider {
    val commandType: DotnetCommandType

    val toolResolver: ToolResolver

    val targetArguments: Sequence<TargetArguments>

    val environmentBuilders: Sequence<EnvironmentBuilder>

    val resultsAnalyzer: ResultsAnalyzer
}