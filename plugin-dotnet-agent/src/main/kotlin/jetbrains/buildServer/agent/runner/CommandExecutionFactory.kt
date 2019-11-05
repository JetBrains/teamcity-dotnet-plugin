package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.rx.Observer

interface CommandExecutionFactory {
    fun create(commandLine: CommandLine, eventObserver: Observer<CommandResultEvent>): CommandExecution
}