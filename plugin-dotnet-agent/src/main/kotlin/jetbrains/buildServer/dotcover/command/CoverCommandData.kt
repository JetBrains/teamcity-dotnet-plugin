package jetbrains.buildServer.dotcover.command

import jetbrains.buildServer.agent.CommandLine

data class CoverCommandData(
    val baseCommandLine: CommandLine,
    val configFilePath: String
)
