package jetbrains.buildServer.dotnet

import java.util.*

interface ResultsAnalyzer {
    fun analyze(exitCode: Int, result: EnumSet<CommandResult>): EnumSet<CommandResult>
}