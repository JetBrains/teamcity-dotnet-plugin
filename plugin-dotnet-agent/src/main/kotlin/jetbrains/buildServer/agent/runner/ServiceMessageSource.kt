package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.rx.Observable

interface ServiceMessageSource : Observable<ServiceMessage> {}