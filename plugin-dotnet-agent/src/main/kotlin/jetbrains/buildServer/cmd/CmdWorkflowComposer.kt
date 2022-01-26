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

package jetbrains.buildServer.cmd

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.util.OSType

class CmdWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _virtualContext: VirtualContext,
        private val _cannotExecute: CannotExecute)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                loop@ for (baseCommandLine in workflow.commandLines) {
                    when(baseCommandLine.executableFile.extension().toLowerCase()) {
                        "cmd", "bat" -> {
                            if(_virtualContext.targetOSType != OSType.WINDOWS) {
                                _cannotExecute.writeBuildProblemFor(baseCommandLine.executableFile)
                                break@loop
                            }
                            else yield(CommandLine(
                                    baseCommandLine,
                                    TargetType.Host,
                                    Path( "cmd.exe"),
                                    baseCommandLine.workingDirectory,
                                    getArguments(baseCommandLine).toList(),
                                    baseCommandLine.environmentVariables,
                                    baseCommandLine.title))
                        }
                        else -> yield(baseCommandLine)
                    }
                }
            })

    private fun getArguments(commandLine: CommandLine) = sequence {
        yield(CommandLineArgument("/D"))
        yield(CommandLineArgument("/C"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}