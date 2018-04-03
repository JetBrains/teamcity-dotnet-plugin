package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import java.util.*

interface DotnetWorkflowAnalyzer {
    fun registerResult(context: DotnetWorkflowAnalyzerContext, result: EnumSet<CommandResult>, exitCode: Int)

    fun summarize(context: DotnetWorkflowAnalyzerContext)
}