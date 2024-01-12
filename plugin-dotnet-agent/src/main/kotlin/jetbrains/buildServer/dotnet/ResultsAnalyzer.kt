

package jetbrains.buildServer.dotnet

interface ResultsAnalyzer {
    fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult>
}