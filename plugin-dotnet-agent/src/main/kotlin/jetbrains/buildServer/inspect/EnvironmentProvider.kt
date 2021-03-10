package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

interface EnvironmentProvider {
    fun getEnvironmentVariables(): Sequence<CommandLineEnvironmentVariable>
}