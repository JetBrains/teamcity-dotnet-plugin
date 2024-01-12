

package jetbrains.buildServer.dotnet.test.dotnet.commands.test

import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer

class TestsResultsAnalyzerStub : ResultsAnalyzer {
    override fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult> =
            setOf(CommandResult.Success)
}