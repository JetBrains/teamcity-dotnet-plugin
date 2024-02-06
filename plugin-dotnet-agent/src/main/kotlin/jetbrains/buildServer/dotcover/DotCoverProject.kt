package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotcover.command.DotCoverCommandType
import java.io.File

data class DotCoverProject(
        val dotCoverCommandType: DotCoverCommandType,
        val coverCommandData: CoverCommandData? = null,
        val mergeCommandData: MergeCommandData? = null,
        val reportCommandData: ReportCommandData? = null) {

        data class CoverCommandData(
                val commandLineToCover: CommandLine,
                val configFile: Path,
                val snapshotFile: Path
        )

        data class MergeCommandData(
                val sourceFiles: List<File>,
                val outputFile: File
        )

        data class ReportCommandData(
                val sourceFile: File,
                val outputFile: File
        )
}