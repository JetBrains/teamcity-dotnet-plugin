package jetbrains.buildServer.dotnet

import java.util.*

interface DotnetWorkflowAnalyzer {
    fun registerResult(context: DotnetWorkflowAnalyzerContext, result: Set<CommandResult>, exitCode: Int)

    fun summarize(context: DotnetWorkflowAnalyzerContext)
}