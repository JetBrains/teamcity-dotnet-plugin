package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.ServiceMessageSource
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import jetbrains.buildServer.rx.*

class FailedTestSourceImpl(
        private val _serviceMessageSource: ServiceMessageSource)
    : FailedTestSource {
    override fun subscribe(observer: Observer<Unit>): Disposable =
        _serviceMessageSource
                .filter { ServiceMessageTypes.TEST_FAILED.equals(it.messageName, true) }
                .first()
                .map { Unit }
                .subscribe(observer)
}