package jetbrains.buildServer.dotnet

import java.util.*

class DotnetWorkflowAnalyzerContext {
    private val _commandResults = mutableListOf<EnumSet<CommandResult>>()

    val results: Sequence<EnumSet<CommandResult>> get() = _commandResults.asSequence()

    fun addResult(result: EnumSet<CommandResult>) {
        _commandResults.add(result)
    }
}