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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.commands.resolution.DotnetCommandsResolver
import jetbrains.buildServer.rx.*
import java.io.File

class DotnetWorkflowComposer(
    private val _pathsService: PathsService,
    private val _defaultEnvironmentVariables: EnvironmentVariables,
    private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
    private val _failedTestSource: FailedTestSource,
    private val _commandRegistry: CommandRegistry,
    private val _parametersService: ParametersService,
    private val _virtualContext: VirtualContext,
    private val _dotnetCommandsResolver: DotnetCommandsResolver,
) : SimpleWorkflowComposer {
    private val verbosityLevel get() = _parametersService
        .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
        ?.trim()
        ?.let { Verbosity.tryParse(it) }

    private val dotnetCommands get() = _dotnetCommandsResolver.resolve()

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow = sequence {
        val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
        val virtualWorkingDirectory = Path(_virtualContext.resolvePath(workingDirectory.path))

        val versions = mutableMapOf<String, Version>()
        var virtualDotnetExecutable: Path? = null
        val analyzerContext = DotnetWorkflowAnalyzerContext()
        val verbosityLevel = verbosityLevel

        for (dotnetCommand in dotnetCommands) {
            if (context.status != WorkflowStatus.Running) {
                break
            }

            val executable = dotnetCommand.toolResolver.executable
            var virtualPath = executable.virtualPath

            var version: Version? = versions[executable.path.path]
            if (version == null) {
                val toolState = ToolState(
                    executable,
                    observer { virtualDotnetExecutable = it },
                    observer {
                        version = it
                        versions[executable.path.path] = it
                    }
                )

                yieldAll(dotnetCommand.toolResolver.toolStateWorkflowComposer.compose(context, toolState).commandLines)
            }

            virtualPath = virtualDotnetExecutable ?: virtualPath

            val commandContext = DotnetBuildContext(
                workingDirectory = ToolPath(workingDirectory, virtualWorkingDirectory),
                command = dotnetCommand,
                toolVersion = version ?: Version.Empty,
                verbosityLevel = verbosityLevel
            )
            val args = dotnetCommand.getArguments(commandContext).toList()
            val result = mutableSetOf<CommandResult>()

            disposableOf(
                // Subscribe command results observer
                context.subscribe(dotnetCommand.resultsObserver),
                // Build an environment
                dotnetCommand.environmentBuilders.map { it.build(commandContext) }.toDisposable(),
                // Subscribe for failed tests
                _failedTestSource.subscribe { result += CommandResult.FailedTests },
                // Subscribe for an exit code
                context.toExitCodes().subscribe {
                    val commandResult = dotnetCommand.resultsAnalyzer.analyze(it, result)
                    _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, it)
                    if (commandResult.contains(CommandResult.Fail)) {
                        context.abort(BuildFinishedStatus.FINISHED_FAILED)
                    }
                }
            ).use {
                _commandRegistry.register(commandContext)
                yield(
                    CommandLine(
                        baseCommandLine = null,
                        target = getTargetType(dotnetCommand),
                        executableFile = virtualPath,
                        workingDirectory = commandContext.workingDirectory.path,
                        arguments = args,
                        environmentVariables = _defaultEnvironmentVariables.getVariables(commandContext.toolVersion).toList(),
                        title = getTitle(virtualPath, args, dotnetCommand.title),
                        description = getDescription(commandContext),
                    )
                )
            }
        }

        _dotnetWorkflowAnalyzer.summarize(analyzerContext)
    }.let(::Workflow)

    private fun getTargetType(command: DotnetCommand) = when {
        command.isAuxiliary -> TargetType.AuxiliaryTool
        else -> TargetType.Tool
    }

    private fun getTitle(executableFile: Path, args: List<CommandLineArgument>, title: String) = sequence {
        if (title.isNotEmpty()) {
            yield(title)
            return@sequence
        }

        yield(File(executableFile.path).nameWithoutExtension)
        yieldAll(args.filter { it.argumentType == CommandLineArgumentType.Mandatory }.map { it.value })
    }.joinToString(" ")

    private fun getDescription(dotnetBuildContext: DotnetBuildContext): List<StdOutText> {
        val description = mutableListOf<StdOutText>()
        when (dotnetBuildContext.command.toolResolver.platform) {
            ToolPlatform.CrossPlatform -> description.add(StdOutText(".NET SDK ", Color.Header))
            else -> {}
        }

        if (dotnetBuildContext.toolVersion != Version.Empty) {
            description.add(StdOutText("${dotnetBuildContext.toolVersion} ", Color.Header))
        }

        return description
    }
}
