package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult

interface ResultsAnalyzer {
    fun isSuccessful(result: CommandLineResult): Boolean
}