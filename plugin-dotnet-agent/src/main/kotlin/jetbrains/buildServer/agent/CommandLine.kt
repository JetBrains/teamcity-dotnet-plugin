package jetbrains.buildServer.agent

import java.io.File

data class CommandLine(
        val target: TargetType,
        val executableFile: File,
        val workingDirectory: File,
        val arguments: List<CommandLineArgument>,
        val environmentVariables: List<CommandLineEnvironmentVariable>) {
}