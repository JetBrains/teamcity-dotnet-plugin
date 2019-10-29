package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.*

interface WorkflowContext: Observable<CommandResultEvent> {
    val status: WorkflowStatus

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}

fun Observable<CommandResultEvent>.toExitCodes(): Observable<Int> = this.ofType<CommandResultEvent, CommandResultExitCode>().map { it.exitCode }

fun Observable<CommandResultEvent>.toOutput(): Observable<String> = this.ofType<CommandResultEvent, CommandResultOutput>().map { it.output }

fun Observable<CommandResultEvent>.toErrors(): Observable<String> = this.ofType<CommandResultEvent, CommandResultError>().map { it.error }