

package jetbrains.buildServer.agent

data class CommandResultExitCode(val exitCode: Int, override val SourceId: Long = -1): CommandResultEvent