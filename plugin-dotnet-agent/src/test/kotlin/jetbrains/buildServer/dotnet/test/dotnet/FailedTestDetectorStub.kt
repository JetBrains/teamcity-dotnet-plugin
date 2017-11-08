package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.dotnet.FailedTestDetector

class FailedTestDetectorStub(private val _hasFailedTest: Boolean): FailedTestDetector {
    override fun hasFailedTest(result: CommandLineResult) = _hasFailedTest
}