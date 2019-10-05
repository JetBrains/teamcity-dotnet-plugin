package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText
import java.io.File

data class CommandLine(
        val target: TargetType,
        val executableFile: Path,
        val workingDirectory: Path,
        val arguments: List<CommandLineArgument> = emptyList(),
        val environmentVariables: List<CommandLineEnvironmentVariable> = emptyList(),
        val title: String = "",
        val description: List<StdOutText> = emptyList())