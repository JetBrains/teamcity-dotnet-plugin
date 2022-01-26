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

package jetbrains.buildServer.custom

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.util.OSType
import java.io.File

class ExecutableWorkflowComposer(
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _dotnetStateWorkflowComposer: ToolStateWorkflowComposer,
        private val _virtualContext: VirtualContext,
        private val _environmentVariables: EnvironmentVariables,
        private val _cannotExecute: CannotExecute)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Host

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                var dotnetExecutableFile: String? = null
                for (commandLine in workflow.commandLines) {
                    if (context.status != WorkflowStatus.Running) {
                        break
                    }

                    val executableFile = commandLine.executableFile.path
                    val executableFileExtension =
                            if (executableFile.isNotBlank()) File(executableFile).extension.trim().toLowerCase()
                            else ""

                    when {
                        // native windows
                        "exe".equals(executableFileExtension, true) || "com".equals(executableFileExtension, true) -> {
                            if (_virtualContext.targetOSType != OSType.WINDOWS) {
                                _cannotExecute.writeBuildProblemFor(commandLine.executableFile)
                            } else yield(commandLine)
                        }

                        // dotnet host
                        executableFile.isBlank() || "dll".equals(executableFileExtension, true) -> {
                            var cmdArgs =
                                    if (executableFile.isNotBlank()) listOf(CommandLineArgument(executableFile, CommandLineArgumentType.Target)) + commandLine.arguments
                                    else commandLine.arguments

                            var description: List<StdOutText> = commandLine.description
                            val defaultDotnetExecutableFile = _dotnetToolResolver.executable
                            if (_virtualContext.isVirtual && dotnetExecutableFile == null) {
                                var toolState = ToolState(
                                        defaultDotnetExecutableFile,
                                        observer<Path> { dotnetExecutableFile = it.path }
                                )

                                yieldAll(_dotnetStateWorkflowComposer.compose(context, toolState).commandLines)
                            }

                            yield(CommandLine(
                                    commandLine,
                                    commandLine.target,
                                    Path(dotnetExecutableFile ?: defaultDotnetExecutableFile.path.path),
                                    commandLine.workingDirectory,
                                    cmdArgs,
                                    commandLine.environmentVariables,
                                    commandLine.title,
                                    description))
                        }
                        else -> {
                            yield(commandLine)
                        }
                    }
                }
            })
}