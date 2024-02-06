package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotCoverReportCommandLineBuilder(
    pathsService: PathsService,
    virtualContext: VirtualContext,
    parametersService: ParametersService,
    fileSystemService: FileSystemService
) : DotCoverCommandLineBuilderBase(pathsService, virtualContext, parametersService, fileSystemService) {

    override val type: DotCoverCommandType get() = DotCoverCommandType.Report

    override fun buildCommand(
        executableFile: Path,
        environmentVariables: List<CommandLineEnvironmentVariable>,
        configFilePath: String,
        baseCommandLine: CommandLine?
    ): CommandLine {
        return CommandLine(
            baseCommandLine = null,
            target = TargetType.CodeCoverageProfiler,
            executableFile = executableFile,
            workingDirectory = workingDirectory,
            arguments = createArguments(configFilePath).toList(),
            environmentVariables = environmentVariables,
            title = "dotCover report"
        )
    }

    private fun createArguments(configFilePath: String): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("report", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument(configFilePath, CommandLineArgumentType.Target))

        logFileName?.let { yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural)) }
    }
}