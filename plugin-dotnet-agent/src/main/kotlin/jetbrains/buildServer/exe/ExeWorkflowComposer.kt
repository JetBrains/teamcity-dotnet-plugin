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

package jetbrains.buildServer.exe

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class ExeWorkflowComposer(
        private val _virtualContext: VirtualContext,
        private val _loggerService: LoggerService)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                for (baseCommandLine in workflow.commandLines) {
                    acceptExecutable(baseCommandLine.executableFile)
                    yield(baseCommandLine)
                }
            })

    private fun acceptExecutable(executableFile: Path) =
            when(executableFile.extension().toLowerCase()) {
            "exe", "com" -> {
                if(_virtualContext.targetOSType != OSType.WINDOWS) {
                    _loggerService.writeWarning("The Windows executable file \"$executableFile\" cannot be executed on this agent, please use an appropriate agents requirement (or a docker image).")
                    false
                }
                else true
            }
            else -> false
        }
}