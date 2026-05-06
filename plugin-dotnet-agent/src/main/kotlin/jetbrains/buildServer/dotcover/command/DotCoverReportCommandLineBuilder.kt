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
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotcover.tool.DotCoverAgentTool
import jetbrains.buildServer.dotcover.tool.DotCoverToolType
import jetbrains.buildServer.dotnet.CoverageConstants
import kotlin.sequences.forEach

class DotCoverReportCommandLineBuilder(
    pathsService: PathsService,
    virtualContext: VirtualContext,
    fileSystemService: FileSystemService,
    private val _dotCoverAgentTool: DotCoverAgentTool,
    private val _parametersService: ParametersService,
    private val _argumentsService: ArgumentsService,
) : DotCoverCommandLineBuilderBase(pathsService, virtualContext, _parametersService, fileSystemService, _dotCoverAgentTool) {

    override val type: DotCoverCommandType get() = DotCoverCommandType.Report

    override fun buildCommand(
        executableFile: Path,
        environmentVariables: List<CommandLineEnvironmentVariable>,
        commandLineParamsFilePath: String,
        baseCommandLine: CommandLine?
    ): CommandLine {
        return CommandLine(
            baseCommandLine = null,
            target = TargetType.CodeCoverageProfiler,
            executableFile = executableFile,
            workingDirectory = workingDirectory,
            arguments = createArguments(commandLineParamsFilePath).toList(),
            environmentVariables = environmentVariables,
            title = "dotCover report"
        )
    }

    private fun createArguments(configFilePath: String): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("report", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(commandLineParametersFilePrefix + configFilePath, CommandLineArgumentType.Target))

        if (_dotCoverAgentTool.type == DotCoverToolType.CrossPlatformV3) {
            logFileName?.let { logFileName ->
                yield(CommandLineArgument("${argumentPrefix}log-file", CommandLineArgumentType.Infrastructural))
                yield(CommandLineArgument(logFileName, CommandLineArgumentType.Infrastructural))
            }
        } else {
            logFileName?.let {
                yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural))
            }
        }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }
}