package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLine
import java.io.File

data class DotCoverProject(
        val commandLineToGetCoverage: CommandLine,
        val configFile: File,
        val snapshotFile: File)