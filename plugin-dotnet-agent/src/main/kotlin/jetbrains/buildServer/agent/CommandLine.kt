package jetbrains.buildServer.agent

import java.io.File

data class CommandLine(
        val target: TargetType,
        val executableFile: Path,
        val workingDirectory: Path,
        val arguments: List<CommandLineArgument>,
        val environmentVariables: List<CommandLineEnvironmentVariable>)