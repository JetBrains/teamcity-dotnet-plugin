package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineResult
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes

class FailedTestDetectorImpl : FailedTestDetector {
    override fun hasFailedTest(text: String): Boolean =
            text.trimStart().startsWith(FailedTestMarker)

    companion object {
        val FailedTestMarker = "${ServiceMessage.SERVICE_MESSAGE_START}${ServiceMessageTypes.TEST_FAILED}"
    }
}