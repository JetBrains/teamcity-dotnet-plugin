

package jetbrains.buildServer.dotnet

class DotnetWorkflowAnalyzerContext {
    private val _commandResults = mutableListOf<Set<CommandResult>>()

    val results: Sequence<Set<CommandResult>> get() = _commandResults.asSequence()

    fun addResult(result: Set<CommandResult>) {
        _commandResults.add(result)
    }
}