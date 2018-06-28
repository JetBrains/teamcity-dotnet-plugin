package jetbrains.buildServer.dotnet

import java.util.*
import jetbrains.buildServer.agent.runner.*


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

        if(!_buildOptions.failBuildOnExitCode) {
            return EnumSet.of(CommandResult.Success)
        }
        else  {
            return EnumSet.of(CommandResult.Fail)
        }
    }
}