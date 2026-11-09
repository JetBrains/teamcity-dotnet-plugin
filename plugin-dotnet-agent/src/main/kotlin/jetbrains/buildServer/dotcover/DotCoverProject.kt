package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.dotcover.command.DotCoverCommandType

data class DotCoverProject(
    val dotCoverCommandType: DotCoverCommandType,
    val coverCommandData: CoverCommandData? = null,
    val mergeCommandData: MergeCommandData? = null,
    val reportCommandData: ReportCommandData? = null
) {
    data class CoverCommandData(
        val commandLineToCover: CommandLine,

        /**
         * File to hold part or all of the cli parameters.
         * Depending on the dotCover version, the file may be either XML config or
         * response file.
         *
         * @see DotCoverRunConfigFileSerializer
         * @see DotCoverResponseFileSerializer
         */
        val commandLineParamsFile: Path,

        /**
         * Path to the resulting coverage snapshot.
         */
        val snapshotFile: Path
    )

    data class MergeCommandData(
        val sourceFiles: List<Path>,
        val outputFile: Path
    )

    data class ReportCommandData(
        val sourceFile: Path,
        val outputFile: Path
    )
}