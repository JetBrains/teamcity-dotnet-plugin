package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.FailedTestDetector
import jetbrains.buildServer.dotnet.TestsResultsAnalyzer

class TestsResultsAnalyzerStub(private val _hasFailedTest: Boolean): TestsResultsAnalyzer {
    override fun isSuccessful(result: CommandLineResult): Boolean {
        return !_hasFailedTest
    }
}