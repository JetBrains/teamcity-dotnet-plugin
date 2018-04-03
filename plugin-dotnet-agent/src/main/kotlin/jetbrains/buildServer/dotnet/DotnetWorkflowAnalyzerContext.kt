package jetbrains.buildServer.dotnet

import java.util.*

class DotnetWorkflowAnalyzerContext {
    private val _commandResults = mutableListOf<EnumSet<CommandResult>>()

    public val results: Sequence<EnumSet<CommandResult>> get() = _commandResults.asSequence()

    public fun addResult(result: EnumSet<CommandResult>) {
        _commandResults.add(result)
    }
}