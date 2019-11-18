package jetbrains.buildServer.agent

data class CommandResultOutput(val output: String, val attributes: MutableCollection<CommandResultAttribute> = mutableSetOf<CommandResultAttribute>()): CommandResultEvent