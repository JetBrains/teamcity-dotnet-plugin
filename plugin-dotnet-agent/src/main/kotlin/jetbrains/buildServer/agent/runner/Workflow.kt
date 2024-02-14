package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.CommandLine

data class Workflow(val commandLines: Sequence<CommandLine> = emptySequence()) {
    constructor(vararg commandLines: CommandLine): this(commandLines.asSequence())
}