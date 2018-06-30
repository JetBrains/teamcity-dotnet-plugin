@file:Suppress("MoveLambdaOutsideParentheses")

package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ObservablesTest {
    @Test
    fun shouldCreate() {
        // Given

        // When
        val source = observableOf<Int> {
            it.onNext(1)
            it.onNext(2)
            it.onComplete()
            emptyDisposable()
        }

        // Then
        val actual = source.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>()))
    }

    @Test
    fun shouldCreateWhenEmpty() {
        // Given

        // When
        val source = observableOf<Int> {
            it.onComplete()
            emptyDisposable()
        }

        // Then
        val actual = source.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                listOf(NotificationCompleted.shared<Int>()))
    }

    @Test
    fun shouldCreateWhenError() {
        // Given

        // When
        val source = observableOf<Int> {
            it.onNext(1)
            it.onNext(2)
            it.onError(error)
            emptyDisposable()
        }

        // Then
        val actual = source.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                listOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)))
    }

    @Test
    fun shouldCreateEmpty() {
        // Given

        // When
        val source = emptyObservable<Int>()

        // Then
        Assert.assertEquals(
                source.materialize().toSequence().toList(),
                listOf(NotificationCompleted.shared<Int>()))
    }

    @DataProvider
    fun testDataMap(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext("1"), NotificationNext("2"), NotificationCompleted.shared<String>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        listOf(NotificationNext("1"), NotificationNext("2"), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<String>())))
    }

    @Test(dataProvider = "testDataMap")
    fun shouldMap(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<String>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actualNotifications = source.map { it.toString() }

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(actual, expectedNotifications)
    }

    @DataProvider
    fun testDataFilter(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationNext(3), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(3), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<Int>())))
    }

    @Test(dataProvider = "testDataFilter")
    fun shouldFilter(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actualNotifications = source.filter { it != 2 }

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(actual, expectedNotifications)
    }

    @DataProvider
    fun testDataUntil(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationCompleted.shared<Int>()),
                        listOf(NotificationCompleted.shared<Int>())))
    }

    @Test(dataProvider = "testDataUntil")
    fun shouldUntil(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actualNotifications = source.until { it == 2 }

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(actual, expectedNotifications)
    }

    @DataProvider
    fun testDataTake(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        0..1,
                        listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        1..2,
                        listOf(NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        1..1,
                        listOf(NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0..9,
                        listOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0..1,
                        listOf(NotificationNext(1), NotificationNext(2), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0..0,
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        -2..-1,
                        listOf(NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationCompleted.shared<Int>()),
                        0..1,
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationCompleted.shared<Int>()),
                        0..1,
                        listOf(NotificationCompleted.shared<Int>())))
    }

    @Test(dataProvider = "testDataTake")
    fun shouldTake(data: Observable<Notification<Int>>, range: IntRange, expectedNotifications: List<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actualNotifications = source.take(range)

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                expectedNotifications)
    }

    @DataProvider
    fun testDataFirst(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())),
                arrayOf(
                        observableOf(NotificationError<Int>(error)),
                        listOf(NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationCompleted.shared<Int>()),
                        listOf(NotificationNext(1), NotificationCompleted.shared<Int>())))
    }

    @Test(dataProvider = "testDataFirst")
    fun shouldFirst(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actualNotifications = source.first()

        // Then
        val actual = actualNotifications.materialize().toSequence().toList()
        Assert.assertEquals(
                actual,
                expectedNotifications)
    }

    @Test
    fun shouldShare() {
        // Given
        val observers = mutableListOf<Observer<Int>>()

        // When
        val source = observableOf<Int> {
            observers.add(it)
            it.onComplete()
            emptyDisposable()
        }.share()

        source.subscribe({}).use {
            source.subscribe({}).use {
            }
        }

        // Then
        Assert.assertEquals(observers.size, 1)
    }

    @Test
    fun shouldTrack() {
        // Given
        val subscribes = mutableListOf<Boolean>()
        val unsubscribes = mutableListOf<Boolean>()

        // When
        observableOf<Int> {
            it.onComplete()
            emptyDisposable()
        }
                .track({ subscribes.add(it) }, { unsubscribes.add(it) })
                .subscribe({})
                .use { }

        // Then
        Assert.assertEquals(subscribes, listOf(false, true))
        Assert.assertEquals(unsubscribes, listOf(false, true))
    }

    companion object {
        private val error = Exception()
    }
}