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

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.ofType

interface WorkflowContext: Observable<CommandResultEvent> {
    val status: WorkflowStatus

    fun abort(buildFinishedStatus: BuildFinishedStatus)
}

fun Observable<CommandResultEvent>.toExitCodes(): Observable<Int> = this.ofType<CommandResultEvent, CommandResultExitCode>().map { it.exitCode }

fun Observable<CommandResultEvent>.toOutput(): Observable<String> = this.ofType<CommandResultEvent, CommandResultOutput>().map { it.output }

fun Observable<CommandResultEvent>.toErrors(): Observable<String> = this.ofType<CommandResultEvent, CommandResultError>().map { it.error }