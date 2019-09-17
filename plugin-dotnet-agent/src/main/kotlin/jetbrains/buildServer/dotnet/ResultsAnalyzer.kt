package jetbrains.buildServer.dotnet

import java.util.*

interface ResultsAnalyzer {
    fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult>
}