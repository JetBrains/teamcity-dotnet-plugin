package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observer

interface CommandExecutionAdapter: CommandExecution, BuildProgressLoggerAware {
    fun initialize(commandLine: CommandLine, eventObserver: Observer<CommandResultEvent>)
}