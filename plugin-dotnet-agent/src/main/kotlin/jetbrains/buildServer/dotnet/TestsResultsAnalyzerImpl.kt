package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.agent.runner.BuildOptions
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class TestsResultsAnalyzerImpl(
        private val _buildOptions: BuildOptions)
    : FailedTestDetector, ResultsAnalyzer {

    override fun hasFailedTest(text: String): Boolean =
            text.contains(FailedTestMarker)

    override fun isSuccessful(result: CommandLineResult): Boolean {
        if (result.exitCode == 0) {
            return true
        }

        if (result.exitCode > 0) {
            return result.standardOutput.map { hasFailedTest(it) }.filter { it }.any()
        }

        return !_buildOptions.failBuildOnExitCode
    }

    companion object {
        val FailedTestMarker = "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED}"
    }
}