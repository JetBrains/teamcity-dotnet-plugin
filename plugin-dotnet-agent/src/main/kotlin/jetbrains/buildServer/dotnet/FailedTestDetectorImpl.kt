package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class FailedTestDetectorImpl : FailedTestDetector {
    override fun hasFailedTest(result: CommandLineResult): Boolean =
        result.standardOutput.filter { it.trimStart().startsWith(FailedTestMarker) }.any()

    companion object {
        val FailedTestMarker = "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED}"
    }
}