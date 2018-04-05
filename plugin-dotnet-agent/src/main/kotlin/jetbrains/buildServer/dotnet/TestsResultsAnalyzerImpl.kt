package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import java.util.*

class TestsResultsAnalyzerImpl(
        private val _buildOptions: BuildOptions)
    : FailedTestDetector, ResultsAnalyzer {

    override fun hasFailedTest(text: String): Boolean =
            text.contains(FailedTestMarker)

    override fun analyze(result: CommandLineResult): EnumSet<CommandResult> {
        if (result.exitCode == 0) {
            return EnumSet.of(CommandResult.Success)
        }

        if (result.exitCode > 0) {
            if(result.standardOutput.map { hasFailedTest(it) }.filter { it }.any()) {
                return EnumSet.of(CommandResult.Success, CommandResult.FailedTests)
            }
        }

        if(!_buildOptions.failBuildOnExitCode) {
            return EnumSet.of(CommandResult.Success)
        }
        else  {
            return EnumSet.of(CommandResult.Fail)
        }
    }

    companion object {
        val FailedTestMarker = "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED}"
    }
}