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

package jetbrains.buildServer.cmd

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Workflow
import jetbrains.buildServer.agent.runner.WorkflowComposer
import jetbrains.buildServer.agent.runner.WorkflowContext
import jetbrains.buildServer.util.OSType

class CmdWorkflowComposer(
        private val _argumentsService: ArgumentsService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext)
    : WorkflowComposer<Unit> {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            when (_virtualContext.targetOSType) {
                OSType.WINDOWS -> {
                    Workflow(sequence {
                        var cmdExecutable: Path? = null
                        for (baseCommandLine in workflow.commandLines) {
                            when (baseCommandLine.executableFile.extension().toLowerCase()) {
                                "cmd", "bat" -> {
                                    yield(CommandLine(
                                            baseCommandLine,
                                            TargetType.Host,
                                            cmdExecutable ?: Path( "cmd.exe"),
                                            baseCommandLine.workingDirectory,
                                            getArguments(baseCommandLine).toList(),
                                            baseCommandLine.environmentVariables,
                                            baseCommandLine.title,
                                            baseCommandLine.description))
                                }
                                else -> yield(baseCommandLine)
                            }
                        }
                    })
                }
                else -> workflow
            }

    private fun getArguments(commandLine: CommandLine) = sequence {
        yield(CommandLineArgument("/D"))
        yield(CommandLineArgument("/C"))
        val args = sequenceOf(commandLine.executableFile.path).plus(commandLine.arguments.map { it.value }).map { _virtualContext.resolvePath(it) }
        yield(CommandLineArgument("\"${_argumentsService.combine(args)}\"", CommandLineArgumentType.Target))
    }
}