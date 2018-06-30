package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageHandler
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister
import jetbrains.buildServer.rx.*

class ServiceMessageSourceImpl(
        private val _serviceMessagesRegister: ServiceMessagesRegister)
    : ServiceMessageSource, ServiceMessageHandler {
    private val _subject: Subject<ServiceMessage> = subjectOf()
    private val _sharedSource: Observable<ServiceMessage> = _subject
            .track(
                    { if (it) activate() },
                    { if (!it) deactivate() })
            .share()

    override fun subscribe(observer: Observer<ServiceMessage>): Disposable =
            _sharedSource.subscribe(observer)

    override fun handle(serviceMessage: ServiceMessage) =
            _subject.onNext(serviceMessage)

    private fun activate() =
            serviceMessages.forEach { _serviceMessagesRegister.registerHandler(it, this) }

    private fun deactivate() =
            serviceMessages.forEach { _serviceMessagesRegister.removeHandler(it) }

    companion object {
        internal val serviceMessages = sequenceOf(ServiceMessageTypes.TEST_FAILED)
    }
}