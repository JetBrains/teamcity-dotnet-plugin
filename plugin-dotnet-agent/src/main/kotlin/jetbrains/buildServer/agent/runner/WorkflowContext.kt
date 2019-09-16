package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observable

interface WorkflowContext: Observable<CommandResultEvent> {
    val status: WorkflowStatus

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}