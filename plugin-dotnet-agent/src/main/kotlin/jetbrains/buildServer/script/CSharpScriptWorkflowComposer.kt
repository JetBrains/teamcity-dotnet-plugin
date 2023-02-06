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

package jetbrains.buildServer.script

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class CSharpScriptWorkflowComposer(
        private val _buildInfo: BuildInfo,
        private val _commandLineFactory: CommandLineFactory,
        private val _buildOptions: BuildOptions,
        private val _loggerService: LoggerService)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
            when (_buildInfo.runType) {
                ScriptConstants.RUNNER_TYPE -> Workflow(createCommandLines(context))
                else -> Workflow()
            }

    private fun createCommandLines(context: WorkflowContext) = sequence<CommandLine> {
        var exitCode = 0;
        val commandLine = _commandLineFactory.create()
        disposableOf(
                context.filter { it.SourceId == commandLine.Id }.toExitCodes().subscribe { exitCode = it; }
        ).use {
            yield(commandLine)
        }

        if (exitCode != 0 && _buildOptions.failBuildOnExitCode) {
            _loggerService.writeBuildProblem("csi_exit_code$exitCode", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code $exitCode")
        }
    }
}