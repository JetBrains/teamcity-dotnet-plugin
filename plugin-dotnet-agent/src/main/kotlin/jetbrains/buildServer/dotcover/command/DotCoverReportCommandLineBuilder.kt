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
        coverCommandData: CoverCommandData?,
        mergeCommandData: MergeCommandData?,
        reportCommandData: ReportCommandData?
    ): CommandLine {
        return CommandLine(
            baseCommandLine = null,
            target = TargetType.CodeCoverageProfiler,
            executableFile = executableFile,
            workingDirectory = workingDirectory,
            arguments = createArguments(reportCommandData!!.sourceFile, reportCommandData.outputFile).toList(),
            environmentVariables = environmentVariables,
            title = "dotCover report"
        )
    }

    private fun createArguments(source: File,
                                output: File
    ): Sequence<CommandLineArgument> = sequence {
        yield(CommandLineArgument("report", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument("${argumentPrefix}Source=${source.absolutePath}"))
        yield(CommandLineArgument("${argumentPrefix}Output=${output.absolutePath}"))
        yield(CommandLineArgument("${argumentPrefix}ReportType=TeamCityXML"))

        logFileName?.let { yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural)) }
    }
}