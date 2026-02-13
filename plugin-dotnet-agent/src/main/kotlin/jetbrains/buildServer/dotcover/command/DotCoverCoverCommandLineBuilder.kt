package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.MonoConstants
import jetbrains.buildServer.mono.MonoToolProvider

class DotCoverCoverCommandLineBuilder(
    pathsService: PathsService,
    virtualContext: VirtualContext,
    private val _dotCoverAgentTool: DotCoverAgentTool,
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _argumentsService: ArgumentsService,
    private val _buildStepContext: BuildStepContext,
    private val _monoToolProvider: MonoToolProvider,
) : DotCoverCommandLineBuilderBase(pathsService, virtualContext, _parametersService, _fileSystemService, _dotCoverAgentTool) {

    override val type: DotCoverCommandType get() = DotCoverCommandType.Cover

    override fun buildCommand(
        executableFile: Path,
        environmentVariables: List<CommandLineEnvironmentVariable>,
        commandLineParamsFilePath: String,
        baseCommandLine: CommandLine?
    ): CommandLine {
        return CommandLine(
            baseCommandLine = baseCommandLine!!,
            target = TargetType.CodeCoverageProfiler,
            executableFile = executableFile,
            workingDirectory = baseCommandLine.workingDirectory,
            arguments = createArguments(commandLineParamsFilePath, baseCommandLine.executableFile).toList(),
            environmentVariables = environmentVariables,
            title = baseCommandLine.title
        )
    }

    private fun createArguments(commandLineParamsFilePath: String, executableFile: Path): Sequence<CommandLineArgument> = sequence {
        val monoExecutable = runCatching { _monoToolProvider.getPath(
            MonoConstants.RUNNER_TYPE,
            _buildStepContext.runnerContext.build,
            _buildStepContext.runnerContext
        ) }.getOrNull()
        if (executableFile.path == monoExecutable) {
            yield(CommandLineArgument("cover-mono", CommandLineArgumentType.Mandatory))
        } else {
            yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        }
        yield(CommandLineArgument(commandLineParametersFilePrefix + commandLineParamsFilePath, CommandLineArgumentType.Target))
        if (_dotCoverAgentTool.type != DotCoverToolType.CrossPlatformV3) {
            // Not supported in DotCover 2025.2+
            yield(CommandLineArgument("${argumentPrefix}ReturnTargetExitCode"))
            yield(CommandLineArgument("${argumentPrefix}AnalyzeTargetArguments=false"))

            // Syntax of 2025.1 or earlier
            logFileName?.let {
                yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural))
            }
        } else {
            // Syntax of 2025.2+
            logFileName?.let { logFileName ->
                yield(CommandLineArgument("${argumentPrefix}log-file", CommandLineArgumentType.Infrastructural))
                yield(CommandLineArgument(logFileName, CommandLineArgumentType.Infrastructural))
            }
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }
}