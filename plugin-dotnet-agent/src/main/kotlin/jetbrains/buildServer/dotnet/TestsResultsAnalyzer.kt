package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult

interface TestsResultsAnalyzer {
    fun isSuccessful(result: CommandLineResult): Boolean
}