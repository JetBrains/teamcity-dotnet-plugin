/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("MoveLambdaOutsideParentheses")

package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import jetbrains.buildServer.rx.NotificationCompleted.Companion.completed
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ObservablesTest {
    @Test
    fun shouldCreate() {
        // Given

        // When
        val source = observable<Int> {
            onNext(1)
            onNext(2)
            onComplete()
            emptyDisposable()
        }

        // Then
        assertEquals(source, observableOf(1, 2))
    }

    @Test
    fun shouldCreateWhenEmpty() {
        // Given

        // When
        val source = observable<Int> {
            onComplete()
            emptyDisposable()
        }

        // Then
        assertEquals(source, emptyObservable())
    }

    @Test
    fun shouldCreateWhenError() {
        // Given

        // When
        val source = observable<Int> {
            onNext(1)
            onNext(2)
            onError(error)
            emptyDisposable()
        }

        // Then
        assertEquals(source, observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)).dematerialize())
    }

    @Test
    fun shouldCreateEmpty() {
        // Given

        // When
        val source = emptyObservable<Int>()

        // Then
        assertEquals(source, observableOf(completed<Int>()).dematerialize())
    }

    @DataProvider
    fun testDataMap(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>()),
                        observableOf(NotificationNext("1"), NotificationNext("2"), completed<String>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        observableOf(NotificationNext("1"), NotificationNext("2"), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(completed<Int>()),
                        observableOf(completed<String>())))
    }

    @Test(dataProvider = "testDataMap")
    fun shouldMap(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<String>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.map{ it.toString() }

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataReduce(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        observableOf(NotificationNext(6), completed<Long>())),
                arrayOf(
                        observableOf(NotificationError<Int>(error)),
                        observableOf(NotificationError<Long>(error))),
                arrayOf(
                        observableOf(NotificationNext(33), completed()),
                        observableOf(NotificationNext(33), completed<Long>())),
                arrayOf(
                        observableOf(completed<Int>()),
                        observableOf(NotificationNext(0), completed<Long>())))
    }

    @Test(dataProvider = "testDataReduce")
    fun shouldReduce(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Long>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.reduce(0) { total, next -> total + next}

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataFilter(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), completed()),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError(error)),
                        observableOf(NotificationNext(1), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(1), NotificationNext(3), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(3), completed<Int>())),
                arrayOf(
                        observableOf(completed()),
                        observableOf(completed<Int>())))
    }

    @Test(dataProvider = "testDataFilter")
    fun shouldFilter(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.filter{ it != 2 }

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())

    }

    @DataProvider
    fun testDataUntil(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError(error)),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationError(error)),
                        observableOf(NotificationNext(1), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(completed()),
                        observableOf(completed<Int>())))
    }

    @Test(dataProvider = "testDataUntil")
    fun shouldUntil(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.until{ it == 2 }

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataTake(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        0..1,
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        1..2,
                        observableOf(NotificationNext(2), NotificationNext(3), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        1..1,
                        observableOf(NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0 .. 9,
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0..1,
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError<Int>(error)),
                        0..0,
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        -2..-1,
                        observableOf(completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), completed<Int>()),
                        0 .. 1,
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(completed<Int>()),
                        0 .. 1,
                        observableOf(completed<Int>())))
    }

    @Test(dataProvider = "testDataTake")
    fun shouldTake(data: Observable<Notification<Int>>, range: IntRange, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.take(range)

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataFirst(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationError(error)),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(NotificationError(error)),
                        observableOf(NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), completed<Int>())))
    }

    @Test(dataProvider = "testDataFirst")
    fun shouldFirst(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual= source.first()

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataLast(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(3), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationError(error)),
                        observableOf(NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationError(error)),
                        observableOf(NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), completed<Int>())),
                arrayOf(
                        observableOf(completed()),
                        observableOf(completed<Int>())))
    }

    @Test(dataProvider = "testDataLast")
    fun shouldLast(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.last()

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testDataCount(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>()),
                        observableOf(NotificationNext(3L), completed<Long>())),
                arrayOf(
                        observableOf(NotificationError<Int>(error)),
                        observableOf(NotificationError<Long>(error))),
                arrayOf(
                        observableOf(NotificationNext(33), completed()),
                        observableOf(NotificationNext(1L), completed<Long>())),
                arrayOf(
                        observableOf(completed<Int>()),
                        observableOf(NotificationNext(0L), completed<Long>())))
    }

    @Test(dataProvider = "testDataCount")
    fun shouldCount(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Long>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.count()

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @Test
    fun shouldShare() {
        // Given
        val observers = mutableListOf<Observer<Int>>()

        // When
        val source = observable<Int> {
            observers.add(this)
            onComplete()
            emptyDisposable()
        }.share()

        source.subscribe {}.use {
            source.subscribe { }.use {
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
        observable<Int> {
            onComplete()
            emptyDisposable()
        }
                .track({subscribes.add(it)}, {unsubscribes.add(it)})
                .subscribe {}
                .use { }

        // Then
        Assert.assertEquals(subscribes, listOf(false, true))
        Assert.assertEquals(unsubscribes, listOf(false, true))
    }

    @DataProvider
    fun testDistinct(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(1), NotificationNext(2), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(1), NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(1), NotificationNext(2), NotificationNext(2), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(1), NotificationNext(2), NotificationNext(2), NotificationNext(1), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), completed<Int>())),
                arrayOf(
                        observableOf(completed()),
                        observableOf(completed<Int>())),
                arrayOf(
                        observableOf(NotificationError(error)),
                        observableOf(NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed()),
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), completed<Int>())))
    }

    @Test(dataProvider = "testDistinct")
    fun shouldDistinct(data: Observable<Notification<Int>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual= source.distinct()

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    @DataProvider
    fun testTypeOf(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf<Any>(
                        observableOf<Any>(NotificationNext<Any>(1), NotificationNext<Any>("2"), NotificationNext<Any>(3), completed<Any>()),
                        observableOf<Notification<Int>>(NotificationNext(1), NotificationNext(3), completed<Int>())),
                arrayOf<Any>(
                        observableOf<Notification<String>>(NotificationNext("1"), NotificationNext("2"), completed()),
                        observableOf(completed<Int>())),
                arrayOf<Any>(
                        observableOf<Notification<Any>>(completed()),
                        observableOf(completed<Int>())),
                arrayOf<Any>(
                        observableOf<Notification<Any>>(NotificationError(error)),
                        observableOf(NotificationError<Int>(error))))
    }

    @Test(dataProvider = "testTypeOf")
    fun shouldTypeOf(data: Observable<Notification<Any>>, expectedNotifications: Observable<Notification<Int>>) {
        // Given
        val source = data.dematerialize()

        // When
        val actual = source.ofType<Any, Int>()

        // Then
        assertEquals(actual, expectedNotifications.dematerialize())
    }

    companion object {
        private val error = Exception()
    }
}