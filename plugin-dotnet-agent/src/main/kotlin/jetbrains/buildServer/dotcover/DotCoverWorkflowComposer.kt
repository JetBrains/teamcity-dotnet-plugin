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

package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _coverageFilterProvider: CoverageFilterProvider,
        private val _virtualContext: VirtualContext,
        private val _environmentVariables: EnvironmentVariables)
    : SimpleWorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, state:Unit, workflow: Workflow): Workflow {
        if (!dotCoverEnabled) {
            return workflow
        }

        val dotCoverPath: String?
        val dotCoverExecutablePath: File
        try {
            dotCoverPath = _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            if (dotCoverPath.isNullOrBlank()) {
                return workflow
            }

            dotCoverExecutablePath = File(dotCoverPath, dotCoverExecutableFile)
            if (_virtualContext.targetOSType != OSType.WINDOWS && !_fileSystemService.isExists(dotCoverExecutablePath)) {
                throw RunBuildException("Cross-Platform dotCover is required.")
            }
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        if (!_fileSystemService.isExists(dotCoverExecutablePath)) {
            throw RunBuildException("dotCover was not found: $dotCoverExecutablePath")
        }

        return Workflow(sequence {
            var dotCoverHome = false
            for (baseCommandLine in workflow.commandLines) {
                if (!baseCommandLine.chain.any { it.target == TargetType.Tool }) {
                    yield(baseCommandLine)
                    continue
                }

                val configFile = _pathsService.getTempFileName(DotCoverConfigExtension)
                val snapshotFile = _pathsService.getTempFileName(DotCoverSnapshotExtension)
                val virtualWorkingDirectory = Path(_virtualContext.resolvePath(baseCommandLine.workingDirectory.path))
                val virtualconfigFilePath = Path(_virtualContext.resolvePath(configFile.path))
                val virtualSnapshotFilePath = Path(_virtualContext.resolvePath(snapshotFile.path))

                val dotCoverProject = DotCoverProject(
                        CommandLine(
                                baseCommandLine,
                                baseCommandLine.target,
                                baseCommandLine.executableFile,
                                virtualWorkingDirectory,
                                baseCommandLine.arguments,
                                baseCommandLine.environmentVariables,
                                baseCommandLine.title,
                                baseCommandLine.description
                        ),
                        virtualconfigFilePath,
                        virtualSnapshotFilePath)

                _fileSystemService.write(configFile) {
                    _dotCoverProjectSerializer.serialize(dotCoverProject, it)
                }

                _loggerService.writeTraceBlock("dotCover settings").use {
                    val args = _argumentsService.combine(baseCommandLine.arguments.map { it.value }.asSequence())
                    _loggerService.writeTrace("Command line:")
                    _loggerService.writeTrace("  \"${baseCommandLine.executableFile.path}\" $args")

                    _loggerService.writeTrace("Filters:")
                    for (filter in _coverageFilterProvider.filters) {
                        _loggerService.writeTrace("  $filter")
                    }

                    _loggerService.writeTrace("Attribute Filters:")
                    for (filter in _coverageFilterProvider.attributeFilters) {
                        _loggerService.writeTrace("  $filter")
                    }
                }

                context.toExitCodes().subscribe {
                    if (_fileSystemService.isExists(snapshotFile)) {
                        // Overrides the dotCover home path once
                        if (!dotCoverHome) {
                            _loggerService.writeMessage(DotCoverServiceMessage(Path(dotCoverPath)))
                            dotCoverHome = true
                        }

                        // The snapshot path should be virtual because of the docker wrapper converts it back
                        _loggerService.importData(DotCoverDataProcessorType, virtualSnapshotFilePath, DotCoverToolName)
                    }
                }.use {
                    yield(CommandLine(
                            baseCommandLine,
                            target,
                            Path(_virtualContext.resolvePath(dotCoverExecutablePath.path)),
                            baseCommandLine.workingDirectory,
                            createArguments(dotCoverProject).toList(),
                            baseCommandLine.environmentVariables + _environmentVariables.getVariables(),
                            baseCommandLine.title))
                }
            }
        })
    }

    private val dotCoverEnabled
        get(): Boolean {
            _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_TYPE)?.let {
                if (it == CoverageConstants.PARAM_DOTCOVER) return true
            }

            _parametersService.tryGetParameter(ParameterType.Runner, "dotNetCoverage.dotCover.enabled")?.let {
                if (it.trim().toBoolean()) return true
            }

            return false
        }

    private fun createArguments(dotCoverProject: DotCoverProject) = sequence {
        yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(dotCoverProject.configFile.path, CommandLineArgumentType.Target))
        yield(CommandLineArgument("${argumentPrefix}ReturnTargetExitCode"))
        yield(CommandLineArgument("${argumentPrefix}AnalyzeTargetArguments=false"))
        _parametersService.tryGetParameter(ParameterType.Configuration, CoverageConstants.PARAM_DOTCOVER_LOG_PATH)?.let {
            var argPrefix = when(_virtualContext.targetOSType) {
                OSType.WINDOWS -> "/"
                else -> "--"
            }

            val logFileName = _virtualContext.resolvePath(_fileSystemService.generateTempFile(File(it), "dotCover", ".log").canonicalPath)
            yield(CommandLineArgument("${argPrefix}LogFile=${logFileName}", CommandLineArgumentType.Infrastructural))
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }

    private val dotCoverExecutableFile get() =
        when(_virtualContext.targetOSType) {
            OSType.WINDOWS -> "dotCover.exe"
            else -> "dotCover.sh"
        }

    private val argumentPrefix get () =
        when(_virtualContext.targetOSType) {
            OSType.WINDOWS -> "/"
            else -> "--"
        }

    companion object {
        internal const val DotCoverDataProcessorType = "dotNetCoverage"
        internal const val DotCoverToolName = "dotcover"
        internal const val DotCoverConfigExtension = "dotCover.xml"
        internal const val DotCoverSnapshotExtension = ".dcvr"
    }
}