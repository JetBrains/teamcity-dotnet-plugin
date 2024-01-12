

package jetbrains.buildServer.dotnet

interface DotnetWorkflowAnalyzer {
    fun registerResult(context: DotnetWorkflowAnalyzerContext, result: Set<CommandResult>, exitCode: Int)

    fun summarize(context: DotnetWorkflowAnalyzerContext)
}