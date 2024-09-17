package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.rx.Observer

interface DotnetCommand : ArgumentsProvider {
    val toolResolver: ToolResolver

    val commandType: DotnetCommandType

    val command: Sequence<String>

    val isAuxiliary: Boolean

    val title: String

    val targetArguments: Sequence<TargetArguments>

    val environmentBuilders: List<EnvironmentBuilder>

    val resultsAnalyzer: ResultsAnalyzer

    val resultsObserver: Observer<CommandResultEvent>
}