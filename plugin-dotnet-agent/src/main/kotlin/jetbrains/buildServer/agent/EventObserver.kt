package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Disposable

interface EventObserver {
    fun subscribe(sources: EventSources): Disposable
}