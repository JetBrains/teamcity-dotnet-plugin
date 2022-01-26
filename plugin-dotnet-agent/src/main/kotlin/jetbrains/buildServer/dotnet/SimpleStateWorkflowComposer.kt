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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.observer

class SimpleStateWorkflowComposer(
        private val _virtualContext: VirtualContext,
        private val _pathResolverWorkflowComposers: List<PathResolverWorkflowComposer>)
    : ToolStateWorkflowComposer {

    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: ToolState, workflow: Workflow): Workflow = Workflow(
            sequence {
                val executable = state.executable
                var virtualPath: Path? = null
                if (_virtualContext.isVirtual && executable.homePaths.isEmpty()) {
                    // Getting executable
                    val pathResolverState = PathResolverState(
                            executable.virtualPath,
                            observer<Path> {
                                if (virtualPath == null && it.path.isNotBlank()) {
                                    virtualPath = it
                                    state.virtualPathObserver.onNext(it)
                                }
                            }
                    )

                    for (pathResolverWorkflowFactory in _pathResolverWorkflowComposers) {
                        yieldAll(pathResolverWorkflowFactory.compose(context, pathResolverState).commandLines)
                    }
                }

                state.versionObserver?.onNext(Version.Empty)
            }
    )
}