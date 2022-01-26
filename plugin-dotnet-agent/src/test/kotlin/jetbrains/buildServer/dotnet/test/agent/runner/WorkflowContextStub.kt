/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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