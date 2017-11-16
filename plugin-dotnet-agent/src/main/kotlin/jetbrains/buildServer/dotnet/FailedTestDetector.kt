package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult

interface FailedTestDetector {
    fun hasFailedTest(text: String): Boolean
}