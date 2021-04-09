package jetbrains.buildServer.agent

class EventObservers(private val _observers: List<EventObserver>): Sequence<EventObserver> {
    override fun iterator(): Iterator<EventObserver> = _observers.iterator()
}