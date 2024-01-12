

package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.dotnet.CommandResult
import jetbrains.buildServer.dotnet.ResultsAnalyzer

class TestsResultsAnalyzerImpl(
    private val _buildOptions: BuildOptions
) : ResultsAnalyzer {
    override fun analyze(exitCode: Int, result: Set<CommandResult>): Set<CommandResult> {
        if (exitCode == 0) {
            return setOf(CommandResult.Success)
        }

        if (exitCode > 0 && result.contains(CommandResult.FailedTests)) {
            return result + CommandResult.Success
        }

        return if (!_buildOptions.failBuildOnExitCode) {
            setOf(CommandResult.Success)
        } else {
            setOf(CommandResult.Fail)
        }
    }
}