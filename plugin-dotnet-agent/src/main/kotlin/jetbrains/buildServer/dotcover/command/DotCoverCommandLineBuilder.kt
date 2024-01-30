package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandLineEnvironmentVariable
import jetbrains.buildServer.agent.Path

interface DotCoverCommandLineBuilder {

    val type: DotCoverCommandType

    fun buildCommand(
        executableFile: Path,
        environmentVariables: List<CommandLineEnvironmentVariable>,
        coverCommandData: CoverCommandData? = null,
        mergeCommandData: MergeCommandData? = null,
        reportCommandData: ReportCommandData? = null
    ): CommandLine
}