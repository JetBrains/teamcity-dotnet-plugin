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

package jetbrains.buildServer.dotnet.test.rx

import jetbrains.buildServer.rx.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SubjectsTest {
    @DataProvider
    fun subjectData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>()),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>(), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>())),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error))),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error), NotificationNext(4)),
                        listOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error))),
                arrayOf(
                        emptyObservable<Int>(),
                        emptyList<Int>()))
    }

    @Test(dataProvider = "subjectData")
    fun shouldProvideSubject(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val subject = subjectOf<Int>()
        val actualNotifications = mutableListOf<Notification<Int>>()
        subject.materialize().subscribe { actualNotifications += it }.use {
            // When
            data.dematerialize().subscribe(subject)

            // Then
            Assert.assertEquals(
                    actualNotifications,
                    expectedNotifications)
        }
    }

    @DataProvider
    fun emptySubjectData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>()),
                        emptyList<Notification<Int>>()),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationCompleted.completed<Int>(), NotificationNext(4)),
                        emptyList<Notification<Int>>()),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error)),
                        emptyList<Notification<Int>>()),
                arrayOf(
                        observableOf(NotificationNext(1), NotificationNext(2), NotificationNext(3), NotificationError<Int>(error), NotificationNext(4)),
                        emptyList<Notification<Int>>()),
                arrayOf(
                        emptyObservable<Int>(),
                        emptyList<Int>()))
    }

    @Test(dataProvider = "emptySubjectData")
    fun shouldProvideEmptySubject(data: Observable<Notification<Int>>, expectedNotifications: List<Notification<Int>>) {
        // Given
        val subject = emptySubject<Int>()
        val actualNotifications = mutableListOf<Notification<Int>>()
        subject.materialize().subscribe { actualNotifications += it }.use {
            // When
            data.dematerialize().subscribe(subject)

            // Then
            Assert.assertEquals(
                    actualNotifications,
                    expectedNotifications)
        }
    }

    companion object {
        private val error = Exception()
    }

}