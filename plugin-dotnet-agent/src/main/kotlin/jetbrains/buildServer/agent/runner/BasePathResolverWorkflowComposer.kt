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
import jetbrains.buildServer.rx.distinct
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.use

class BasePathResolverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext)
    : PathResolverWorkflowComposer {

    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: PathResolverState, workflow: Workflow) = Workflow (
            sequence {
                context
                        .toOutput()
                        .distinct()
                        .filter { it.endsWith(state.pathToResolve.path) }
                        .map { Path(it) }
                        .subscribe(state.virtualPathObserver)
                        .use {
                            yield(CommandLine(
                                    null,
                                    TargetType.SystemDiagnostics,
                                    state.commandToResolve,
                                    Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                                    listOf(CommandLineArgument(state.pathToResolve.path, CommandLineArgumentType.Target)),
                                    emptyList<CommandLineEnvironmentVariable>(),
                                    "get ${state.pathToResolve.path}"))
                        }
            }
    )
}