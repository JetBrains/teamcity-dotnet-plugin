

package jetbrains.buildServer.agent

data class CommandLineArgument(
    val value: String,
    val argumentType: CommandLineArgumentType = CommandLineArgumentType.Secondary,
)