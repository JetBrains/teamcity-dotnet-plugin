package jetbrains.buildServer.agent.runner.serviceMessages

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage

class TestRetrySupportServiceMessage(
    enabled: Boolean
) : ServiceMessage("testRetrySupport", mapOf("enabled" to enabled.toString()))