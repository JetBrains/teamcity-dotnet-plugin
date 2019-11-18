package jetbrains.buildServer.agent

data class CommandResultError(val error: String, val attributes: MutableCollection<CommandResultAttribute> = mutableSetOf<CommandResultAttribute>()): CommandResultEvent