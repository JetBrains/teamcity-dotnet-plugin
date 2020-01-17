/*
 * Copyright 2000-2020 JetBrains s.r.o.
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
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.rx.use

class CrossPlatformWorkflowFactory(
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext,
        private val _pathResolverWorkflowFactories: List<PathResolverWorkflowFactory>,
        private val _versionParser: VersionParser,
        private val _defaultEnvironmentVariables: EnvironmentVariables)
    : WorkflowFactory<CrossPlatformWorkflowState> {
    override fun create(context: WorkflowContext, state: CrossPlatformWorkflowState): Workflow = Workflow(
            sequence {
                val executable = state.executable
                var virtualPath: Path? = null
                val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
                if (_virtualContext.isVirtual && executable.homePaths.isEmpty()) {
                    // Getting dotnet executable
                    val pathResolverState = PathResolverState(
                            executable.virtualPath,
                            observer<Path> {
                                if (virtualPath == null && it.path.isNotBlank()) {
                                    virtualPath = it
                                    state.virtualPathObserver.onNext(it)
                                }
                            }
                    )

                    for (pathResolverWorkflowFactory in _pathResolverWorkflowFactories) {
                        yieldAll(pathResolverWorkflowFactory.create(context, pathResolverState).commandLines)
                    }
                }

                // Getting .NET Core version
                context
                        .toOutput()
                        .map { _versionParser.parse(listOf(it)) }
                        .filter { it != Version.Empty }
                        .subscribe(state.versionObserver)
                        .use {
                            yield(
                                    CommandLine(
                                            null,
                                        TargetType.SystemDiagnostics,
                                        virtualPath ?: executable.virtualPath,
                                        workingDirectory,
                                        DotnetWorkflowComposer.VersionArgs,
                                        _defaultEnvironmentVariables.getVariables(Version.Empty).toList(),
                                    "dotnet --version",
                                        listOf(StdOutText("Getting the .NET SDK version"))))
                        }
            }
    )
}