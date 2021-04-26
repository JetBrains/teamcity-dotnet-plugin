package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.CommandLineEnvironmentVariable

class DupFinderEnvironmentProvider: EnvironmentProvider {
    override fun getEnvironmentVariables() = emptySequence<CommandLineEnvironmentVariable>()
}