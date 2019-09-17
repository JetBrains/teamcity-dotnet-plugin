package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.BuildOptions
import java.util.*

class ResultsAnalyzerImpl(private val _buildOptions: BuildOptions) : ResultsAnalyzer {
    override fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult> =
            if (exitCode == 0 || !_buildOptions.failBuildOnExitCode) setOf(CommandResult.Success) else setOf(CommandResult.Fail)
}