

package jetbrains.buildServer.agent

data class CommandResultError(val error: String, val attributes: MutableCollection<CommandResultAttribute> = mutableSetOf<CommandResultAttribute>(), override val SourceId: Long = -1): CommandResultEvent