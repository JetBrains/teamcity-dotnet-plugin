package jetbrains.buildServer.agent

import jetbrains.buildServer.agent.runner.StdOutText

data class CommandLine(
        val baseCommandLine: CommandLine?,
        val target: TargetType,
        val executableFile: Path,
        val workingDirectory: Path,
        val arguments: List<CommandLineArgument> = emptyList(),
        val environmentVariables: List<CommandLineEnvironmentVariable> = emptyList(),
        val title: String = "",
        val description: List<StdOutText> = emptyList())

val CommandLine.chain: Sequence<CommandLine> get() {
    var cur: CommandLine? = this
    return sequence {
        while (cur != null) {
            yield(cur!!)
            cur = cur?.baseCommandLine
        }
    }
}