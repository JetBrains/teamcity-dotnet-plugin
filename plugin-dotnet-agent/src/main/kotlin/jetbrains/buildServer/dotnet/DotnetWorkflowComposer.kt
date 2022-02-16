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

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.agent.Logger
import java.io.File

class DotnetWorkflowComposer(
        private val _pathsService: PathsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        private val _dotnetWorkflowAnalyzer: DotnetWorkflowAnalyzer,
        private val _commandSet: CommandSet,
        private val _failedTestSource: FailedTestSource,
        private val _commandRegistry: CommandRegistry,
        private val _parametersService: ParametersService,
        private val _commandLinePresentationService: CommandLinePresentationService,
        private val _virtualContext: VirtualContext)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow =
            Workflow(sequence {
                val verbosity = _parametersService
                        .tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                        ?.trim()
                        ?.let { Verbosity.tryParse(it) }

                val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
                val virtualWorkingDirectory = Path(_virtualContext.resolvePath(workingDirectory.path))

                var versions = mutableMapOf<String, Version>()
                var virtualDotnetExecutable: Path? = null
                val analyzerContext = DotnetWorkflowAnalyzerContext()
                for (command in _commandSet.commands) {
                    if (context.status != WorkflowStatus.Running) {
                        break
                    }

                    val executable = command.toolResolver.executable
                    var virtualPath = executable.virtualPath

                    var version: Version? = versions[executable.path.path];
                    if (version == null) {
                        var toolState = ToolState(
                                executable,
                                observer<Path> { virtualDotnetExecutable = it },
                                observer<Version> {
                                    version = it
                                    versions[executable.path.path] = it;
                                }
                        )

                        yieldAll(command.toolResolver.toolStateWorkflowComposer.compose(context, toolState).commandLines)
                    }

                    virtualPath = virtualDotnetExecutable ?: virtualPath

                    val dotnetBuildContext = DotnetBuildContext(ToolPath(workingDirectory, virtualWorkingDirectory), command, version ?: Version.Empty, verbosity)
                    val args = dotnetBuildContext.command.getArguments(dotnetBuildContext).toList()
                    val result = mutableSetOf<CommandResult>()
                    disposableOf(
                            // Subscribe command results observer
                            context.subscribe(command.resultsObserver),
                            // Build an environment
                            dotnetBuildContext.command.environmentBuilders.map { it.build(dotnetBuildContext) }.toDisposable(),
                            // Subscribe for failed tests
                            _failedTestSource.subscribe { result += CommandResult.FailedTests },
                            // Subscribe for an exit code
                            context.toExitCodes().subscribe {
                                val commandResult = dotnetBuildContext.command.resultsAnalyzer.analyze(it, result)
                                _dotnetWorkflowAnalyzer.registerResult(analyzerContext, commandResult, it)
                                if (commandResult.contains(CommandResult.Fail)) {
                                    context.abort(BuildFinishedStatus.FINISHED_FAILED)
                                }
                            }
                    ).use {
                        _commandRegistry.register(dotnetBuildContext)
                        yield(CommandLine(
                                null,
                                TargetType.Tool,
                                virtualPath,
                                dotnetBuildContext.workingDirectory.path,
                                args,
                                _defaultEnvironmentVariables.getVariables(dotnetBuildContext.toolVersion).toList(),
                                getTitle(virtualPath, args),
                                getDescription(dotnetBuildContext)))
                    }
                }

                _dotnetWorkflowAnalyzer.summarize(analyzerContext)
            })

    private fun getTitle(executableFile: Path, args: List<CommandLineArgument>) =
        (sequenceOf(File(executableFile.path).nameWithoutExtension) + args.filter { i -> i.argumentType == CommandLineArgumentType.Mandatory }.map { it.value }).joinToString(" ")

    private fun getDescription(dotnetBuildContext: DotnetBuildContext): List<StdOutText> {
        var description = mutableListOf<StdOutText>()
        when (dotnetBuildContext.command.toolResolver.platform) {
            ToolPlatform.CrossPlatform -> description.add(StdOutText(".NET SDK ", Color.Header))
            else -> { }
        }

        if (dotnetBuildContext.toolVersion != Version.Empty) {
            description.add(StdOutText("${dotnetBuildContext.toolVersion} ", Color.Header))
        }

        return description
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetWorkflowComposer::class.java)
        internal val VersionArgs = listOf(CommandLineArgument("--version"))
    }
}