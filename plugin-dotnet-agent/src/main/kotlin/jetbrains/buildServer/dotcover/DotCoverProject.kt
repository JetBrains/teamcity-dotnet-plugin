package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path

data class DotCoverProject(
        val commandLineToCover: CommandLine,
        val configFile: Path,
        val snapshotFile: Path)