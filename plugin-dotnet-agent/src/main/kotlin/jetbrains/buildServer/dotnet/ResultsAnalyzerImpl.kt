package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.BuildOptions
import java.util.*

class ResultsAnalyzerImpl(private val _buildOptions: BuildOptions): ResultsAnalyzer {
    override fun analyze(result: CommandLineResult): EnumSet<CommandResult> =
        if(result.exitCode == 0 || !_buildOptions.failBuildOnExitCode) EnumSet.of(CommandResult.Success) else EnumSet.of(CommandResult.Fail)
}