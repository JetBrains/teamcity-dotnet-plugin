/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.use

class InspectionToolStateWorkflowComposerImpl(
    private val _tool: InspectionTool,
    private val _pathsService: PathsService,
    private val _versionParser: ToolVersionOutputParser
) : InspectionToolStateWorkflowComposer {
    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: InspectionToolState, workflow: Workflow): Workflow = sequence {
        context
            .toOutput()
            .map { _versionParser.parse(listOf(it)) }
            .filter { !it.isEmpty() }
            .subscribe(state.versionObserver)
            .use {
                yield(
                    CommandLine(
                        baseCommandLine = null,
                        target = target,
                        executableFile = state.startCommand.executable,
                        workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath),
                        arguments = state.startCommand.startArguments.plus(CommandLineArgument("--version")),
                        environmentVariables = emptyList(),
                        title = "Getting ${_tool.displayName} version"
                    )
                )
            }
    }.let(::Workflow)
}