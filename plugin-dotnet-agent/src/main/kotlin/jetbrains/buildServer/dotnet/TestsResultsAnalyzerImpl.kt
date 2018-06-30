package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.BuildOptions
import java.util.*


class TestsResultsAnalyzerImpl(
        private val _buildOptions: BuildOptions)
    : ResultsAnalyzer {

    override fun analyze(exitCode: Int, result: EnumSet<CommandResult>): EnumSet<CommandResult> {
        if (exitCode == 0) {
            return EnumSet.of(CommandResult.Success)
        }

        if (exitCode > 0 && result.contains(CommandResult.FailedTests)) {
            result.add(CommandResult.Success)
            return result
        }

        return if (!_buildOptions.failBuildOnExitCode) {
            EnumSet.of(CommandResult.Success)
        } else {
            EnumSet.of(CommandResult.Fail)
        }
    }
}