

package jetbrains.buildServer.dotnet.test.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.agent.runner.WorkflowStatus
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.rx.emptyDisposable

class WorkflowContextStub(override var status: WorkflowStatus, vararg private val _events: CommandResultEvent) : WorkflowContext {
    override fun abort(buildFinishedStatus: BuildFinishedStatus) {
        status = WorkflowStatus.Failed
    }

    override fun subscribe(observer: Observer<CommandResultEvent>): Disposable {
        for (ev in _events) {
            observer.onNext(ev);
        }

        return emptyDisposable()
    }
}