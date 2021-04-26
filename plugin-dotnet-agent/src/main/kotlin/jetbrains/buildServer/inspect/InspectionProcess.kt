package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path

data class InspectionProcess(val executable: Path, val startArguments: Collection<CommandLineArgument> = emptyList())