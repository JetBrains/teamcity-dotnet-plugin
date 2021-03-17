/*
 * Copyright 2000-2021 JetBrains s.r.o.
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
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class InspectionWorkflowComposer(
        private val _tool: InspectionTool,
        private val _toolPathResolver: ProcessResolver,
        private val _argumentsProvider: ArgumentsProvider,
        private val _environmentProvider: EnvironmentProvider,
        private val _outputObserver: OutputObserver,
        private val _configurationFile: ConfigurationFile,
        private val _buildInfo: BuildInfo,
        private val _fileSystemService: FileSystemService,
        private val _pathsService: PathsService,
        private val _loggerService: LoggerService,
        private val _artifacts: ArtifactService,
        private val _virtualContext: VirtualContext)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow) =
            if (_buildInfo.runType == _tool.runnerType) Workflow(createCommandLines(context)) else Workflow()

    private fun createCommandLines(context: WorkflowContext) = sequence<CommandLine> {
        val process = _toolPathResolver.resolve(_tool)
        var args = _argumentsProvider.getArguments(_tool)
        val cmdArgs = sequence<CommandLineArgument> {
            yieldAll(process.startArguments)
            yield(CommandLineArgument("--config=${_virtualContext.resolvePath(args.configFile.absolutePath)}"))
            if (args.debug) {
                yield(CommandLineArgument("--logFile=${_virtualContext.resolvePath(args.logFile.absolutePath)}"))
            }

            yieldAll(args.customArguments)
        }

        val virtualOutputPath = Path(_virtualContext.resolvePath((args.outputFile.absolutePath)))
        createConfigFile(args, virtualOutputPath)
        disposableOf(
                context.toOutput().subscribe(_outputObserver),
                context.toExitCodes().subscribe { exitCode -> onExit(exitCode, context, args, virtualOutputPath) }
        ).use {
            yield(
                    CommandLine(
                            null,
                            target,
                            process.executable,
                            Path(_pathsService.getPath(PathType.Checkout).path),
                            cmdArgs.toList(),
                            _environmentProvider.getEnvironmentVariables().toList()))
        }
    }

    private fun createConfigFile(args: InspectionArguments, virtualOutputPath: Path)=
        _fileSystemService.write(args.configFile) {
            _configurationFile.create(
                    it,
                    virtualOutputPath,
                    args.cachesHome?.let { Path(_virtualContext.resolvePath(it.absolutePath)) },
                    args.debug)
        }

    private fun onExit(exitCode: Int, context: WorkflowContext, args: InspectionArguments, virtualOutputPath: Path) {
        if (args.debug) {
            _artifacts.publish(_tool, args.logFile)
        }

        if (exitCode != 0) {
            _loggerService.buildFailureDescription("${_tool.dysplayName} execution failure.")
            context.abort(BuildFinishedStatus.FINISHED_FAILED)
        } else {
            if (_artifacts.publish(_tool, args.outputFile, Path(_tool.reportArtifactName))) {
                _loggerService.importData(_tool.dataProcessorType, virtualOutputPath)
            } else {
                _loggerService.buildFailureDescription("Output xml from ${_tool.dysplayName} is not found or empty on path ${args.outputFile.canonicalPath}.")
                context.abort(BuildFinishedStatus.FINISHED_FAILED)
            }
        }
    }
}