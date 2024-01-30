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

class DotCoverMergeCommandLineBuilder(
    pathsService: PathsService,
    virtualContext: VirtualContext,
    parametersService: ParametersService,
    fileSystemService: FileSystemService
) : DotCoverCommandLineBuilderBase(pathsService, virtualContext, parametersService, fileSystemService) {

    override val type: DotCoverCommandType get() = DotCoverCommandType.Merge

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
            arguments = createArguments(mergeCommandData!!).toList(),
            environmentVariables = environmentVariables,
            title = "dotCover merge"
        )
    }

    private fun createArguments(mergeCommandData: MergeCommandData): Sequence<CommandLineArgument> = sequence {
        val sources = mergeCommandData.sourceFiles.joinToString(";") { it.absolutePath }

        yield(CommandLineArgument("merge", CommandLineArgumentType.Mandatory))
        yield(CommandLineArgument("${argumentPrefix}Source=${sources}"))
        yield(CommandLineArgument("${argumentPrefix}Output=${mergeCommandData.outputFile.absolutePath}"))

        logFileName?.let { yield(CommandLineArgument("${argumentPrefix}LogFile=${it}", CommandLineArgumentType.Infrastructural)) }
    }
}