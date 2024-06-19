package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.CommandResultAttribute
import jetbrains.buildServer.agent.CommandResultError
import jetbrains.buildServer.agent.CommandResultEvent
import jetbrains.buildServer.agent.CommandResultOutput
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes.TEST_SUITE_FINISHED
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes.TEST_SUITE_STARTED
import jetbrains.buildServer.rx.Observer
import java.text.ParseException

// stdout/stderr messages come twice (TW-43953):
// 1. nunit extension "teamcity-event-listener" produces testStdOut/testStdErr service messages
// 2. TeamCity captures child process stdout
class NUnitOutputDeduplicationObserver : Observer<CommandResultEvent> {
    private var testSuiteCounter = 0
    override fun onNext(value: CommandResultEvent) {
        when (value) {
            is CommandResultOutput -> ServiceMessage.parse(value.output,
                object : ServiceMessageParserCallback {
                    override fun regularText(text: String) {
                        if (testSuiteCounter != 0) {
                            value.attributes.add(CommandResultAttribute.Suppressed)
                        }
                    }

                    override fun serviceMessage(message: ServiceMessage) =
                        message.messageName.let {
                            if (TEST_SUITE_STARTED.equals(it, ignoreCase = true)) {
                                testSuiteCounter++
                            }

                            if (TEST_SUITE_FINISHED.equals(it, ignoreCase = true)) {
                                testSuiteCounter--
                            }
                        }

                    override fun parseException(parseException: ParseException, text: String) = Unit
                })

            is CommandResultError -> {
                if (testSuiteCounter != 0) {
                    value.attributes.add(CommandResultAttribute.Suppressed)
                }
            }
        }
    }

    override fun onError(error: Exception) = Unit

    override fun onComplete() = Unit
}