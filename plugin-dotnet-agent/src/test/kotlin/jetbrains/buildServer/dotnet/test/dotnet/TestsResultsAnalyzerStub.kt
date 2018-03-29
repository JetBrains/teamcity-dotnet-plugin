package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer

class TestsResultsAnalyzerStub(private val _hasFailedTest: Boolean): ResultsAnalyzer {
    override fun isSuccessful(result: CommandLineResult): Boolean {
        return !_hasFailedTest
    }
}