package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer
import java.util.*

class TestsResultsAnalyzerStub : ResultsAnalyzer {
    override fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult> =
            setOf(CommandResult.Success)
}