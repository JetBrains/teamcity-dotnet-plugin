package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

interface EnvironmentVariables {
    fun getVariables(): Sequence<CommandLineEnvironmentVariable>
}