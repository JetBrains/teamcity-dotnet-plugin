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

package jetbrains.buildServer.custom

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.rx.observer
import java.io.File

class CustomCommandWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService,
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _targetService: TargetService,
        private val _dotnetToolResolver: DotnetToolResolver,
        private val _fileSystemService: FileSystemService,
        private val _dotnetStateWorkflowComposer: ToolStateWorkflowComposer,
        private val _virtualContext: VirtualContext)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow) =
            Workflow(sequence {
                if (context.status != WorkflowStatus.Running) {
                    return@sequence
                }

                parameters(DotnetConstants.PARAM_COMMAND)?.let {
                    if (!DotnetCommandType.Custom.id.equals(it, true)) {
                        return@sequence
                    }
                } ?: return@sequence

                val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).path)
                val args = parameters(DotnetConstants.PARAM_ARGUMENTS)?.trim()?.let {
                    _argumentsService.split(it).map { CommandLineArgument(it, CommandLineArgumentType.Custom) }.toList()
                } ?: emptyList<CommandLineArgument>()

                var dotnetExecutableFile: String? = null
                var dotnetDescription: List<StdOutText> = emptyList()
                val targets = _targetService.targets.map { it.target }.toMutableList()
                if (targets.isEmpty()) {
                    // to run dotnet tools
                    targets.add(Path(""))
                }

                for (target in targets) {
                    if (context.status != WorkflowStatus.Running) {
                        break
                    }

                    var executableFile = target.path
                    var executableFileExtension = ""
                    if(executableFile.isNotBlank()) {
                        if (!_fileSystemService.isAbsolute(File(executableFile))) {
                            executableFile = File(workingDirectory.path, executableFile).path
                        }

                        executableFile = _virtualContext.resolvePath(executableFile)
                        executableFileExtension = File(executableFile).extension.toLowerCase().trim()
                    }

                    var cmdArgs = args
                    var description = emptyList<StdOutText>()
                    if(executableFileExtension.isBlank()) {
                        if (executableFile.isNotBlank()) {
                            cmdArgs = listOf(CommandLineArgument(executableFile, CommandLineArgumentType.Target)) + args
                        }

                        val defaultDotnetExecutableFile = _dotnetToolResolver.executable
                        if (_virtualContext.isVirtual && dotnetExecutableFile == null) {
                            var toolState = ToolState(
                                    defaultDotnetExecutableFile,
                                    observer<Path> { dotnetExecutableFile = it.path },
                                    observer<Version> { version ->
                                        dotnetDescription =
                                                if (version != Version.Empty)
                                                    listOf(StdOutText(".NET SDK ", Color.Header), StdOutText("${version} ", Color.Header))
                                                else
                                                    emptyList<StdOutText>()
                                    }
                            )

                            yieldAll(_dotnetStateWorkflowComposer.compose(context, toolState).commandLines)
                        }

                        executableFile = dotnetExecutableFile ?: defaultDotnetExecutableFile.path.path
                        description = dotnetDescription
                    }

                    yield(CommandLine(
                            null,
                            TargetType.Tool,
                            Path(executableFile),
                            workingDirectory,
                            cmdArgs,
                            emptyList<CommandLineEnvironmentVariable>(),
                            "",
                            description))
                }
            })

    private fun parameters(parameterName: String): String? = _parametersService.tryGetParameter(ParameterType.Runner, parameterName)

    private fun parameters(parameterName: String, defaultValue: String): String = _parametersService.tryGetParameter(ParameterType.Runner, parameterName) ?: defaultValue
}