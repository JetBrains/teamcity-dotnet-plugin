@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.rx

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

inline fun <T> buildObservable(crossinline subscriber: Observer<T>.() -> Disposable): Observable<T> =
        object : Observable<T> {
            override fun subscribe(observer: Observer<T>): Disposable = subscriber(observer)
        }

fun <T> observableOf(vararg values: T): Observable<T> =
        if (values.isEmpty()) emptyObservable() else values.asSequence().toObservable()

fun <T> emptyObservable(): Observable<T> =
        buildObservable {
            onComplete()
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

inline fun <T, R> Observable<T>.map(crossinline map: (T) -> R): Observable<R> =
        buildObservable {
            subscribe(
                    { onNext(map(it)) },
                    { onError(it) },
                    { onComplete() })
        }

inline fun <T, R> Observable<T>.reduce(initialValue: R, crossinline operation: (acc: R, T) -> R): Observable<R> =
        buildObservable {
            var accumulator: R = initialValue
            val lockObject = Object()
            subscribe(
                    {
                        synchronized(lockObject) { accumulator = operation(accumulator, it) }
                    },
                    { onError(it) },
                    {
                        synchronized(lockObject) {
                            onNext(accumulator)
                        }

                        onComplete()
                    })
        }

inline fun <T> Observable<T>.filter(crossinline filter: (T) -> Boolean): Observable<T> =
        buildObservable {
            subscribe(
                    { if (filter(it)) onNext(it) },
                    { onError(it) },
                    { onComplete() })
        }

inline fun <T> Observable<T>.until(crossinline completionCondition: (T) -> Boolean): Observable<T> =
        buildObservable {
            val isCompleted = AtomicBoolean(false)
            subscribe(
                    {
                        if (isCompleted.compareAndSet(false, completionCondition(it))) {
                            onNext(it)
                            if (isCompleted.get()) {
                                onComplete()
                            }
                        }
                    },
                    { if (!isCompleted.get()) onError(it) },
                    { if (!isCompleted.get()) onComplete() })
        }

fun <T> Observable<T>.take(range: LongRange): Observable<T> =
        buildObservable {
            val position = AtomicLong(0)
            map { Pair(it, position.getAndIncrement()) }
                    .until { it.second >= range.endInclusive }
                    .filter { range.contains(it.second) }
                    .map { it.first }
                    .subscribe(this)
        }

fun <T> Observable<T>.take(range: IntRange): Observable<T> =
        take(LongRange(range.start.toLong(), range.endInclusive.toLong()))

fun <T> Observable<T>.first(): Observable<T> =
        take(LongRange(0, 0))

fun <T> Observable<T>.last(): Observable<T> =
        buildObservable {
            val lastValue: AtomicReference<T> = AtomicReference()
            subscribe(
                    { lastValue.set(it) },
                    { onError(it) },
                    {
                        lastValue.get()?.let { onNext(it) }
                        onComplete()
                    })
        }

fun <T> Observable<T>.count(): Observable<Long> =
        reduce(0L) { total, _ -> total + 1L }

fun <T> Sequence<T>.toObservable(): Observable<T> =
        buildObservable {
            forEach { onNext(it) }
            onComplete()
            emptyDisposable()
        }

@Throws(InterruptedException::class)
fun <T> Observable<T>.toSequence(timeout: Long): Sequence<T> =
        object : Sequence<T>, Disposable {
            private val subscriptions = mutableListOf<Disposable>()

            override fun dispose() =
                    synchronized(subscriptions) {
                        subscriptions.forEach { it.dispose() }
                    }

            override fun iterator(): Iterator<T> {
                val lockObject = Object()
                val values = mutableListOf<T>()
                var isFinished = false
                val subscription = subscribe(
                        {
                            synchronized(lockObject) {
                                values.add(it)
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
                            when (timeout) {
                                0L -> while (isEmpty()) {
                                    lockObject.wait(0)
                                }
                                else -> if (isEmpty()) {
                                    lockObject.wait(timeout)
                                }
                            }

                            return values.size > 0
                        }
                    }

                    override fun next(): T {
                        var nextItem: T?
                        synchronized(lockObject) {
                            nextItem = values[0]
                            values.removeAt(0)
                        }

                        return nextItem!!
                    }

                    private fun isEmpty() = values.size == 0 && !isFinished
                }
            }
        }


fun <T> Observable<T>.share(): Observable<T> {
    val refCounter = AtomicInteger(0)
    val subject = subjectOf<T>()
    var subscription = emptyDisposable()
    return buildObservable {
        val curSubscription = subject.subscribe(this)
        if (refCounter.incrementAndGet() == 1) {
            synchronized(subject) {
                subscription = subscribe(subject)
            }
        }

        disposableOf {
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

inline fun <T> Observable<T>.track(crossinline onSubscribe: (Boolean) -> Unit, crossinline onUnsubscribe: (Boolean) -> Unit = {}): Observable<T> =
        buildObservable {
            val subscription: Disposable

            try {
                onSubscribe(false)
                subscription = subscribe(this)
            } finally {
                onSubscribe(true)
            }

            disposableOf {
                try {
                    onUnsubscribe(false)
                    subscription.dispose()
                } finally {
                    onUnsubscribe(true)
                }
            }
        }

fun <T> Observable<T>.materialize(): Observable<Notification<T>> =
        buildObservable {
            subscribe(
                    { onNext(NotificationNext(it)) },
                    {
                        onNext(NotificationError(it))
                        onComplete()
                    },
                    {
                        onNext(NotificationCompleted.completed())
                        onComplete()
                    })
        }

fun <T> Observable<Notification<T>>.dematerialize(): Observable<T> =
        buildObservable {
            subscribe {
                when (it) {
                    is NotificationNext<T> -> onNext(it.value)
                    is NotificationError -> onError(it.error)
                    is NotificationCompleted -> onComplete()
                }
            }
        }
