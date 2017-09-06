package jetbrains.buildServer.dotcover

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.runners.*
import java.io.File
import kotlin.coroutines.experimental.buildSequence

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
class DotCoverWorkflowComposer(
        private val _pathsService: PathsService,
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _dotCoverProjectSerializer: DotCoverProjectSerializer,
        private val _loggerService: LoggerService)
    : WorkflowComposer {

    override val target: TargetType
        get() = TargetType.ProfilerOfCodeCoverage

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        if(!workflow.commandLines.any()) {
            throw RunBuildException("This composer should not be a root")
        }

        val dotCoverPath: String?
        val dotCoverExecutableFile: File
        try {
            var dotCoverEnabled = _parametersService.tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_ENABLED)?.equals("true", true) ?: false;
            if (!dotCoverEnabled) {
                return workflow;
            }

            dotCoverPath = _parametersService.tryGetParameter(ParameterType.Runner, DotCoverConstants.PARAM_HOME);
            if (dotCoverPath.isNullOrBlank()) {
                return workflow;
            }

            dotCoverExecutableFile = File(dotCoverPath, DotCoverExecutableFile).absoluteFile;
        }
        catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        if (!_fileSystemService.isExists(dotCoverExecutableFile)) {
            throw RunBuildException("dotCover was not found: ${dotCoverExecutableFile}")
        }

        return Workflow(
                buildSequence {
                    for (commandLineToGetCoverage in workflow.commandLines) {
                        val tempDirectory = _pathsService.getPath(PathType.BuildTemp);
                        val dotCoverProject = DotCoverProject(
                                commandLineToGetCoverage,
                                File(tempDirectory, _pathsService.uniqueName + DotCoverProjectExtension),
                                File(tempDirectory, _pathsService.uniqueName + DotCoverSnapshotExtension))

                        _fileSystemService.write(dotCoverProject.configFile) {
                            _dotCoverProjectSerializer.serialize(dotCoverProject, it)
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

    fun createArguments(dotCoverProject: DotCoverProject): Sequence<CommandLineArgument> = buildSequence {
        yield(CommandLineArgument("cover"))
        yield(CommandLineArgument(dotCoverProject.configFile.absolutePath))
        yield(CommandLineArgument("/ReturnTargetExitCode"))
        yield(CommandLineArgument("/NoCheckForUpdates"))
    }

    companion object {
        internal val DotCoverExecutableFile = "dotCover.exe"
        internal val DotCoverToolName = "dotcover"
        internal val DotCoverProjectExtension = ".dotCover"
        internal val DotCoverSnapshotExtension = ".dcvr"
    }
}