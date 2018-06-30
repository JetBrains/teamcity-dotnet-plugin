@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.rx

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

inline fun <T> observableOf(crossinline subscriber: (Observer<T>) -> Disposable): Observable<T> =
        object : Observable<T> {
            override fun subscribe(observer: Observer<T>): Disposable = subscriber(observer)
        }

fun <T> observableOf(vararg elements: T): Observable<T> =
        if (elements.isEmpty()) emptyObservable() else elements.asSequence().toObservable()

fun <T> emptyObservable(): Observable<T> =
        observableOf {
            it.onComplete()
            emptyDisposable()
        }

inline fun <T> Observable<T>.subscribe(crossinline onNext: (T) -> Unit): Disposable =
        subscribe(object : Observer<T> {
            override fun onNext(value: T) = onNext(value)
            override fun onError(error: Exception) = Unit
            override fun onComplete() = Unit
        })

inline fun <T> Observable<T>.subscribe(crossinline onNext: (T) -> Unit, crossinline onError: (Exception) -> Unit, crossinline onComplete: () -> Unit): Disposable =
        subscribe(object : Observer<T> {
            override fun onNext(value: T) = onNext(value)
            override fun onError(error: Exception) = onError(error)
            override fun onComplete() = onComplete()
        })

inline fun <T, R> Observable<T>.map(crossinline map: (T) -> R): Observable<R> = observableOf { observer ->
    subscribe(
            { observer.onNext(map(it)) },
            { observer.onError(it) },
            { observer.onComplete() })
}

inline fun <T> Observable<T>.filter(crossinline filter: (T) -> Boolean): Observable<T> = observableOf { observer ->
    subscribe(
            { if (filter(it)) observer.onNext(it) },
            { observer.onError(it) },
            { observer.onComplete() })
}

inline fun <T> Observable<T>.until(crossinline completionCondition: (T) -> Boolean): Observable<T> = observableOf { observer ->
    val isCompleted = AtomicBoolean(false)
    subscribe(
            {
                if (isCompleted.compareAndSet(false, completionCondition(it))) {
                    observer.onNext(it)
                    if (isCompleted.get()) {
                        observer.onComplete()
                    }
                }
            },
            { if (!isCompleted.get()) observer.onError(it) },
            { if (!isCompleted.get()) observer.onComplete() })
}

fun <T> Observable<T>.take(range: LongRange): Observable<T> = observableOf {
    val position = AtomicLong(0)
    map { Pair(it, position.getAndIncrement()) }
            .until { it.second >= range.endInclusive }
            .filter { range.contains(it.second) }
            .map { it.first }
            .subscribe(it)
}

fun <T> Observable<T>.take(range: IntRange): Observable<T> = take(LongRange(range.start.toLong(), range.endInclusive.toLong()))

fun <T> Observable<T>.first(): Observable<T> = take(LongRange(0, 0))

fun <T> Sequence<T>.toObservable(): Observable<T> = observableOf { observer ->
    forEach { observer.onNext(it) }
    observer.onComplete()
    emptyDisposable()
}

fun <T> Observable<T>.toSequence(): Sequence<T> =
        object : Sequence<T>, Disposable {
            private val subscriptions = mutableListOf<Disposable>()

            override fun dispose() =
                    synchronized(subscriptions) {
                        subscriptions.forEach { it.dispose() }
                    }

            override fun iterator(): Iterator<T> {
                val lockObject = Object()
                val items = mutableListOf<T>()
                var isFinished = false
                val subscription = subscribe(
                        {
                            synchronized(lockObject) {
                                items.add(it)
                                lockObject.notify()
                            }
                        },
                        {
                            synchronized(lockObject) {
                                isFinished = true
                                lockObject.notifyAll()
                            }
                        },
                        {
                            synchronized(lockObject) {
                                isFinished = true
                                lockObject.notifyAll()
                            }
                        })

                synchronized(subscriptions) {
                    subscriptions.add(subscription)
                }

                return object : Iterator<T> {
                    override fun hasNext(): Boolean {
                        synchronized(lockObject) {
                            while (items.size == 0 && !isFinished) {
                                lockObject.wait()
                            }
                        }

                        return items.size > 0 || !isFinished
                    }

                    override fun next(): T {
                        var nextItem: T? = null
                        synchronized(lockObject) {
                            nextItem = items[0]
                            items.removeAt(0)
                        }

                        return nextItem!!
                    }
                }
            }
        }


fun <T> Observable<T>.share(): Observable<T> {
    val refCounter = AtomicInteger(0)
    val subject = subjectOf<T>()
    var subscription = emptyDisposable()
    return observableOf {
        val curSubscription = subject.subscribe(it)
        if (refCounter.incrementAndGet() == 1) {
            synchronized(subject) {
                subscription = subscribe(subject)
            }
        }

        return@observableOf disposableOf {
            curSubscription.dispose()
            if (refCounter.decrementAndGet() == 0) {
                synchronized(subject) {
                    try {
                        subscription.dispose()
                    } finally {
                        subscription = emptyDisposable()
                    }
                }
            }
        }
    }
}

inline fun <T> Observable<T>.track(crossinline onSubscribe: (Boolean) -> Unit, crossinline onUnsubscribe: (Boolean) -> Unit = {}): Observable<T> {
    return observableOf {
        val subscription: Disposable

        try {
            onSubscribe(false)
            subscription = subscribe(it)
        } finally {
            onSubscribe(true)
        }

        return@observableOf disposableOf {
            try {
                onUnsubscribe(false)
                subscription.dispose()
            } finally {
                onUnsubscribe(true)
            }
        }
    }
}

enum class NotificationKind {
    OnNext,
    OnError,
    OnCompleted
}

@Suppress("unused")
abstract class Notification<T>(val notificationKind: NotificationKind)

data class NotificationNext<T>(val value: T) : Notification<T>(NotificationKind.OnNext)

data class NotificationError<T>(val error: Exception) : Notification<T>(NotificationKind.OnError)

class NotificationCompleted<T> private constructor() : Notification<T>(NotificationKind.OnCompleted) {
    companion object {
        private val sharedObject = NotificationCompleted<Any>()

        fun <T> shared(): NotificationCompleted<T> {
            @Suppress("UNCHECKED_CAST")
            return sharedObject as NotificationCompleted<T>
        }
    }
}

fun <T> Observable<T>.materialize(): Observable<Notification<T>> = observableOf { observer ->
    subscribe(
            { observer.onNext(NotificationNext(it)) },
            {
                observer.onNext(NotificationError(it))
                observer.onComplete()
            },
            {
                observer.onNext(NotificationCompleted.shared())
                observer.onComplete()
            })
}

fun <T> Observable<Notification<T>>.dematerialize(): Observable<T> = observableOf { observer ->
    subscribe {
        when (it) {
            is NotificationNext<T> -> observer.onNext(it.value)
            is NotificationError -> observer.onError(it.error)
            is NotificationCompleted -> observer.onComplete()
        }
    }
}
