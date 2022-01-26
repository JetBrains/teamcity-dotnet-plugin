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

package jetbrains.buildServer.visualStudio

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.TargetService
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class VisualStudioWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _targetService: TargetService,
        private val _toolResolver: ToolResolver,
        private val _virtualContext: VirtualContext)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                if (context.status != WorkflowStatus.Running) {
                    return@sequence
                }

                parameters(DotnetConstants.PARAM_COMMAND)?.let {
                    if (!DotnetCommandType.VisualStudio.id.equals(it, true)) {
                        return@sequence
                    }
                } ?: return@sequence

                val workingDirectory = _pathsService.getPath(PathType.WorkingDirectory)
                val action = parameters(DotnetConstants.PARAM_VISUAL_STUDIO_ACTION)
                        ?: throw RunBuildException("Parameter \"${DotnetConstants.PARAM_VISUAL_STUDIO_ACTION}\" was not found")

                val configItems = listOf(
                        parameters(DotnetConstants.PARAM_CONFIG, ""),
                        parameters(DotnetConstants.PARAM_PLATFORM, ""))
                        .filter { !it.isBlank() }
                var configValue = configItems.joinToString("|")
                if (configItems.size > 1) {
                    configValue = "\"$configValue\""
                }

                val args = parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
                    _argumentsService.split(it).map { CommandLineArgument(it, CommandLineArgumentType.Custom) }.toList()
                } ?: emptyList<CommandLineArgument>()

                val executableFile = Path(_virtualContext.resolvePath(_toolResolver.executableFile.canonicalPath))

                for ((target) in _targetService.targets) {
                    if (context.status != WorkflowStatus.Running) {
                        break
                    }

                    disposableOf(
                            // Subscribe for an exit code
                            context.subscribe {
                                when {
                                    it is CommandResultExitCode -> {
                                        if (it.exitCode != 0) {
                                            _loggerService.writeBuildProblem("visual_studio_exit_code${it.exitCode}", BuildProblemData.TC_EXIT_CODE_TYPE, "Process exited with code ${it.exitCode}")
                                            context.abort(BuildFinishedStatus.FINISHED_FAILED)
                                        }
                                    }
                                }
                            }
                    ).use {
                        yield(CommandLine(
                                null,
                                TargetType.Tool,
                                executableFile,
                                Path(workingDirectory.path),
                                sequence {
                                    yield(CommandLineArgument(target.path, CommandLineArgumentType.Target))
                                    yield(CommandLineArgument("/$action", CommandLineArgumentType.Mandatory))
                                    if (!configValue.isBlank()) {
                                        yield(CommandLineArgument(configValue))
                                    }

                                    yieldAll(args)
                                }.toList(),
                                emptyList<CommandLineEnvironmentVariable>(),
                                DotnetCommandType.VisualStudio.id))
                    }
                }
            })

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}