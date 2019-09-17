package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.subscribe

interface WorkflowContext: Observable<CommandResultEvent> {
    val status: WorkflowStatus

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}

fun WorkflowContext.subscibeForExitCode(handler: (Int) -> Unit ) =
        this.subscribe {
            if (it is CommandResultExitCode) handler(it.exitCode)
        }

fun WorkflowContext.subscibeForOutput(handler: (String) -> Unit ) =
    this.subscribe {
        if (it is CommandResultOutput) handler(it.output)
    }

fun WorkflowContext.subscibeForErrors(handler: (String) -> Unit ) =
        this.subscribe { it ->
            if (it is CommandResultError) handler(it.error)
        }