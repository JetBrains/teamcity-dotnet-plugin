

package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path

data class ToolStartInfo(val executable: Path, val platform: InspectionToolPlatform, val arguments: Collection<CommandLineArgument> = emptyList())