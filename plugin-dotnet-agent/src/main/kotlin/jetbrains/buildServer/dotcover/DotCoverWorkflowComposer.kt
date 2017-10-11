package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class DotCoverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _coverageFilterProvider: CoverageFilterProvider)
    : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.ProfilerOfCodeCoverage

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        if (!dotCoverEnabled) {
            return workflow
        }

        val dotCoverPath: String?
        val dotCoverExecutableFile: File
        try {
            dotCoverPath = _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_HOME)
            if (dotCoverPath.isNullOrBlank()) {
                return workflow
            }

            dotCoverExecutableFile = File(dotCoverPath, DotCoverExecutableFile).absoluteFile
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        if (!_fileSystemService.isExists(dotCoverExecutableFile)) {
            throw RunBuildException("dotCover was not found: $dotCoverExecutableFile")
        }

        var showDiagnostics = false
        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
            Verbosity.tryParse(it)?.let {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when(it) {
                    Verbosity.Detailed, Verbosity.Diagnostic -> {
                        showDiagnostics = true
                    }
                }
            }
        }

        return Workflow(
                buildSequence {
                    for (commandLineToGetCoverage in workflow.commandLines) {
                        val tempDirectory = _pathsService.getPath(PathType.BuildTemp)
                        val dotCoverProject = DotCoverProject(
                                commandLineToGetCoverage,
                                File(tempDirectory, _pathsService.uniqueName + DotCoverProjectExtension),
                                File(tempDirectory, _pathsService.uniqueName + DotCoverSnapshotExtension))

                        _fileSystemService.write(dotCoverProject.configFile) {
                            _dotCoverProjectSerializer.serialize(dotCoverProject, it)
                        }

                        if (showDiagnostics) {
                            _loggerService.onBlock("dotCover Settings").use {
                                val args = _argumentsService.combine(commandLineToGetCoverage.arguments.map { it.value }.asSequence())
                                _loggerService.onStandardOutput("Command line:")
                                _loggerService.onStandardOutput("  \"${commandLineToGetCoverage.executableFile.path}\" $args", Color.Details)

                                _loggerService.onStandardOutput("Filters:")
                                for (filter in _coverageFilterProvider.filters) {
                                    _loggerService.onStandardOutput("  $filter", Color.Details)
                                }

                                _loggerService.onStandardOutput("Attribute Filters:")
                                for (filter in _coverageFilterProvider.attributeFilters) {
                                    _loggerService.onStandardOutput("  $filter", Color.Details)
                                }
                            }
                        }

                        yield(
                                CommandLine(
                                        TargetType.Tool,
                                        dotCoverExecutableFile,
                                        commandLineToGetCoverage.workingDirectory,
                                        createArguments(dotCoverProject).toList(),
                                        commandLineToGetCoverage.environmentVariables))

                        if (!context.lastResult.isCompleted) {
                            return@buildSequence
                        }

                        _loggerService.onMessage(DotCoverServiceMessage(File(dotCoverPath).absoluteFile))
                        _loggerService.onMessage(ImportDataServiceMessage(DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile))
                    }
                }
        )
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

    private fun createArguments(dotCoverProject: DotCoverProject): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument("cover"))
        yield(CommandLineArgument(dotCoverProject.configFile.absolutePath))
        yield(CommandLineArgument("/ReturnTargetExitCode"))
        yield(CommandLineArgument("/NoCheckForUpdates"))
        yield(CommandLineArgument("/AnalyzeTargetArguments=false"))
    }

    companion object {
        internal val DotCoverExecutableFile = "dotCover.exe"
        internal val DotCoverToolName = "dotcover"
        internal val DotCoverProjectExtension = ".dotCover"
        internal val DotCoverSnapshotExtension = ".dcvr"
    }
}