package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.BuildOptions

class ResultsAnalyzerImpl(private val _buildOptions: BuildOptions): ResultsAnalyzer {
    override fun isSuccessful(result: CommandLineResult): Boolean = result.exitCode == 0 || !_buildOptions.failBuildOnExitCode
}