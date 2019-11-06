package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.use
import jetbrains.buildServer.util.OSType
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
        private val _virtualContext: VirtualContext)
    : WorkflowComposer {

    override val target: TargetType = TargetType.CodeCoverageProfiler

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
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
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        if (!_fileSystemService.isExists(dotCoverExecutablePath)) {
            throw RunBuildException("dotCover was not found: $dotCoverExecutablePath")
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
            var deferredServiceMessages = mutableListOf<ServiceMessage>()
            for (baseCommandLine in workflow.commandLines) {
                sendServiceMessages(context, deferredServiceMessages)
                deferredServiceMessages.clear()

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

                if (showDiagnostics) {
                    _loggerService.writeBlock("dotCover Settings").use {
                        val args = _argumentsService.combine(baseCommandLine.arguments.map { it.value }.asSequence())
                        _loggerService.writeStandardOutput("Command line:")
                        _loggerService.writeStandardOutput("  \"${baseCommandLine.executableFile.path}\" $args", Color.Details)

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
                        baseCommandLine,
                        target,
                        Path(_virtualContext.resolvePath(dotCoverExecutablePath.path)),
                        baseCommandLine.workingDirectory,
                        createArguments(dotCoverProject).toList(),
                        baseCommandLine.environmentVariables,
                        baseCommandLine.title,
                        baseCommandLine.description))

                deferredServiceMessages.add(DotCoverServiceMessage(Path((dotCoverPath))))
                deferredServiceMessages.add(ImportDataServiceMessage(DotCoverToolName, virtualSnapshotFilePath))
            }

            if (context.status == WorkflowStatus.Running) {
                sendServiceMessages(context, deferredServiceMessages)
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
        yield(CommandLineArgument("${argumentPrefix}NoCheckForUpdates"))
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
            StringUtil.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }

    private fun sendServiceMessages(context: WorkflowContext, deferredServiceMessages: List<ServiceMessage>) {
        if (context.status == WorkflowStatus.Failed) {
            return
        }

        for (serviceMessage in deferredServiceMessages) {
            _loggerService.writeMessage(serviceMessage)
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
        internal const val DotCoverToolName = "dotcover"
        internal const val DotCoverConfigExtension = "dotCover.xml"
        internal const val DotCoverSnapshotExtension = ".dcvr"
    }
}