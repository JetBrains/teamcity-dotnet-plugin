package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import java.util.*

interface ResultsAnalyzer {
    fun analyze(result: CommandLineResult): EnumSet<CommandResult>
}