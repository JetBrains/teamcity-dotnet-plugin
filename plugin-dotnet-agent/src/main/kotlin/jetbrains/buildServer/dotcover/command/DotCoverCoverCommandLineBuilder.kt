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
import jetbrains.buildServer.dotnet.CoverageConstants

class DotCoverCoverCommandLineBuilder(
    pathsService: PathsService,
    virtualContext: VirtualContext,
    private val _parametersService: ParametersService,
    fileSystemService: FileSystemService,
    private val _argumentsService: ArgumentsService
) : DotCoverCommandLineBuilderBase(pathsService, virtualContext, _parametersService, fileSystemService) {

    override val type: DotCoverCommandType get() = DotCoverCommandType.Cover

    override fun buildCommand(executableFile: Path,
                              environmentVariables: List<CommandLineEnvironmentVariable>,
                              configFilePath: String,
                              baseCommandLine: CommandLine?
    ): CommandLine {
        return CommandLine(
            baseCommandLine = baseCommandLine!!,
            target = TargetType.CodeCoverageProfiler,
            executableFile = executableFile,
            workingDirectory = baseCommandLine.workingDirectory,
            arguments = createArguments(configFilePath).toList(),
            environmentVariables = environmentVariables,
            title = baseCommandLine.title
        )
    }

    private fun createArguments(configFilePath: String): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("cover", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(configFilePath, CommandLineArgumentType.Target))
        yield(CommandLineArgument("${argumentPrefix}ReturnTargetExitCode"))
        yield(CommandLineArgument("${argumentPrefix}AnalyzeTargetArguments=false"))

        logFileName?.let { yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural)) }

        _parametersService.tryGetParameter(ParameterType.Runner, CoverageConstants.PARAM_DOTCOVER_ARGUMENTS)?.let {
            _argumentsService.split(it).forEach {
                yield(CommandLineArgument(it, CommandLineArgumentType.Custom))
            }
        }
    }
}