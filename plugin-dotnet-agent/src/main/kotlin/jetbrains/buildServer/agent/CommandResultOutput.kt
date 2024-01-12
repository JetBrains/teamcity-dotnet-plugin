

package jetbrains.buildServer.agent

data class CommandResultOutput(
    val output: String,
    val attributes: MutableCollection<CommandResultAttribute> = mutableSetOf<CommandResultAttribute>(),
    override val SourceId: Long = -1,
): CommandResultEvent