package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observer

interface DotnetCommand : ArgumentsProvider {
    val commandType: DotnetCommandType

    val toolResolver: ToolResolver

    val targetArguments: Sequence<TargetArguments>

    val environmentBuilders: Sequence<EnvironmentBuilder>

    val resultsAnalyzer: ResultsAnalyzer

    val resultsObserver: Observer<CommandResultEvent>
}