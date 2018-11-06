@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.StringUtil
import java.io.File

class DotCoverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
        private val _loggerService: LoggerService,
        private val _argumentsService: ArgumentsService,
        private val _coverageFilterProvider: CoverageFilterProvider,
        private val _targetRegistry: TargetRegistry)
    : WorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

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
                when (it) {
                    Verbosity.Detailed, Verbosity.Diagnostic -> {
                        showDiagnostics = true
                    }
                }
            }
        }

        return Workflow(sequence {
            var deferredServiceMessages: DeferredServiceMessages? = null

            _targetRegistry.activate(target).use {
                for (commandLineToGetCoverage in workflow.commandLines) {
                    sendServiceMessages(context, deferredServiceMessages)

                    val tempDirectory = _pathsService.getPath(PathType.BuildTemp)
                    val dotCoverProject = DotCoverProject(
                            commandLineToGetCoverage,
                            File(tempDirectory, _pathsService.uniqueName + DotCoverProjectExtension),
                            File(tempDirectory, _pathsService.uniqueName + DotCoverSnapshotExtension))

                    _fileSystemService.write(dotCoverProject.configFile) {
                        _dotCoverProjectSerializer.serialize(dotCoverProject, it)
                    }

                    if (showDiagnostics) {
                        _loggerService.writeBlock("dotCover Settings").use {
                            val args = _argumentsService.combine(commandLineToGetCoverage.arguments.map { it.value }.asSequence())
                            _loggerService.writeStandardOutput("Command line:")
                            _loggerService.writeStandardOutput("  \"${commandLineToGetCoverage.executableFile.path}\" $args", Color.Details)

                            _loggerService.writeStandardOutput("Filters:")
                            for (filter in _coverageFilterProvider.filters) {
                                _loggerService.writeStandardOutput("  $filter", Color.Details)
                            }

                            _loggerService.writeStandardOutput("Attribute Filters:")
                            for (filter in _coverageFilterProvider.attributeFilters) {
                                _loggerService.writeStandardOutput("  $filter", Color.Details)
                            }
                        }
                    }

                    yield(CommandLine(
                            TargetType.Tool,
                            dotCoverExecutableFile,
                            commandLineToGetCoverage.workingDirectory,
                            createArguments(dotCoverProject).toList(),
                            commandLineToGetCoverage.environmentVariables))

                    deferredServiceMessages =
                            DeferredServiceMessages(
                                    context.lastResult,
                                    listOf(
                                            DotCoverServiceMessage(File(dotCoverPath).absoluteFile),
                                            ImportDataServiceMessage(DotCoverToolName, dotCoverProject.snapshotFile.absoluteFile)))
                }
            }

            sendServiceMessages(context, deferredServiceMessages)
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
        yield(CommandLineArgument("cover"))
        yield(CommandLineArgument(dotCoverProject.configFile.absolutePath))
        yield(CommandLineArgument("/ReturnTargetExitCode"))
        yield(CommandLineArgument("/NoCheckForUpdates"))
        yield(CommandLineArgument("/AnalyzeTargetArguments=false"))
        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            StringUtil.split(it).forEach {
                yield(CommandLineArgument(it))
            }
        }
    }

    private fun sendServiceMessages(context: WorkflowContext, deferredServiceMessages: DeferredServiceMessages?) {
        if (context.status == WorkflowStatus.Failed) {
            return
        }

        deferredServiceMessages?.let {
            it.serviceMessages.forEach { _loggerService.writeMessage(it) }
        }
    }

    companion object {
        internal const val DotCoverExecutableFile = "dotCover.exe"
        internal const val DotCoverToolName = "dotcover"
        internal const val DotCoverProjectExtension = ".dotCover"
        internal const val DotCoverSnapshotExtension = ".dcvr"
    }

    private data class DeferredServiceMessages(val result: CommandLineResult, val serviceMessages: List<ServiceMessage>)
}